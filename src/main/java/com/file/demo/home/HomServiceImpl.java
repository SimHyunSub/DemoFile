package com.file.demo.home;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;

import com.file.demo.home.aspect.LogExecutionTime;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class HomServiceImpl implements HomeService {
	
	private final String separator = File.separator;
	private final String FILE_PATH = "C:" + separator + "DEV" + separator + "testResources"+ separator;

	@LogExecutionTime
	@Override
	public void fileHandler(HttpServletResponse response, FileVO vo) {
		
		String fileName = vo.getFileName();
		String encodedFileName = null;
		
		try {
			encodedFileName = URLEncoder.encode(fileName, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.error(e.getMessage(),e);
		}
		
		response.setHeader("Content-Disposition", "attachment;filename=" + encodedFileName + ";");
		log.info("file name = {}, encoded file name = {}, method type = {}", fileName, fileName, vo.getType());
		
		switch (vo.getType()) {
		case DEFAULT:
			fileStream(response, fileName);
			break;
		case DATA_STREAM:
			dataStream(response, fileName);
			break;
		case BUFFERED_STREAM:
			bufferedStream(response, fileName);
			break;
		case BYTE_BUFFER:
			fileChannel(response, fileName);
			break;
		case BYTE_BUFFER_RANDOM_ACCESS:
			fileChannelRandomAccess(response, fileName);
			break;
		default:
			break;
		}
	}

	public void dataStream(HttpServletResponse response, String fileName) {
		
		try (
				DataInputStream is = new DataInputStream(new FileInputStream(FILE_PATH + fileName));
				DataOutputStream os = new DataOutputStream(response.getOutputStream()); 
		) {
			readWrite(is, os);
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public void fileStream(HttpServletResponse response, String fileName) {
		
		try (
				FileInputStream is = new FileInputStream(FILE_PATH + fileName);
				ServletOutputStream os = response.getOutputStream();
		) {
			readWrite(is, os);
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public void bufferedStream(HttpServletResponse response, String fileName) {
		
		try (
				BufferedInputStream is = new BufferedInputStream( new FileInputStream(FILE_PATH + fileName));
				BufferedOutputStream os = new BufferedOutputStream(response.getOutputStream());
		) {
			readWrite(is,os);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public void fileChannel(HttpServletResponse response, String fileName) {
		
		try (
				FileChannel channel = new FileInputStream(FILE_PATH + fileName).getChannel();
				WritableByteChannel outChannel = Channels.newChannel(response.getOutputStream());
		) {

		    int bufferSize = 1024;
		    if (bufferSize > channel.size()) {
		        bufferSize = (int) channel.size();
		    }
		    
		    ByteBuffer buffer = ByteBuffer.allocateDirect( 1000 * bufferSize );
		    while ( channel.read(buffer) != -1 ) {
		    	buffer.flip();
		    	outChannel.write(buffer);
		    	buffer.clear();
			}
		    
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public void fileChannelRandomAccess(HttpServletResponse response, String fileName) {
		
		try (
				FileChannel channel = new RandomAccessFile(FILE_PATH + fileName,"r").getChannel();
				WritableByteChannel outChannel = Channels.newChannel(response.getOutputStream());
		) {
			
			int bufferSize = 1024;
			if (bufferSize > channel.size()) {
				bufferSize = (int) channel.size();
			}
			
			ByteBuffer buffer = ByteBuffer.allocateDirect( 1000 * bufferSize );
			while ( channel.read(buffer) != -1 ) {
				buffer.flip();
				outChannel.write(buffer);
				buffer.clear();
			}
			
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public void readWrite (InputStream is, OutputStream os) throws IOException {

	    int bufferSize = 1024;
	    int available = is.available();
	    if (bufferSize > available) {
	        bufferSize = available;
	    }
	    
		if(bufferSize > 0) {
			byte[] buffer = new byte[ 1000 * bufferSize ];
			int read = 0;
			while ( ( read = is.read( buffer, 0, buffer.length ) ) != -1 ) {
				os.write(buffer, 0, read);
			}
			os.flush();
		}
	}
}
