package com.file.demo.home;

import com.file.demo.home.aspect.LogExecutionTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
@Service
public class HomServiceImpl implements HomeService {
	
	@Value("${user.file.path}")
	private String FILE_PATH;

	@LogExecutionTime
	@Override
	public void fileHandler(HttpServletResponse response, FileVO file) {
		
		String fileName = file.getFileName();
		String encodedFileName = null;
		
		try {
			encodedFileName = URLEncoder.encode(fileName, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.error(e.getMessage(),e);
		}
		
		response.setHeader("Content-Disposition", "attachment;filename=" + encodedFileName + ";");
		log.info("file name = {}, encoded file name = {}, file download type = {}", fileName, fileName, file.getType());
		
		switch (file.getType()) {
		case DEFAULT:
			fileStream(response, file);
			break;
		case BUFFERED_STREAM:
			bufferedStream(response, file);
			break;
		case CHANNEL:
			fileChannel(response, file);
			break;
		case CHANNEL_RANDOM_ACCESS:
			fileChannelRandomAccess(response, file);
			break;
		default:
			break;
		}
	}

	public void fileStream(HttpServletResponse response, FileVO file) {
		
		try (
				FileInputStream is = new FileInputStream(FILE_PATH + file.getFileName());
				ServletOutputStream os = response.getOutputStream();
		) {
			readWrite(is, os, file);
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public void bufferedStream(HttpServletResponse response, FileVO file) {
		
		try (
				BufferedInputStream is = new BufferedInputStream(Files.newInputStream(Paths.get(FILE_PATH + file.getFileName())));
				BufferedOutputStream os = new BufferedOutputStream(response.getOutputStream());
		) {
			readWrite(is, os, file);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public void fileChannel(HttpServletResponse response, FileVO file) {

		try (
				SeekableByteChannel channel = Files.newByteChannel(Paths.get(FILE_PATH + file.getFileName()));
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
		    
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public void fileChannelRandomAccess(HttpServletResponse response, FileVO file) {
		
		try (
				FileChannel channel = new RandomAccessFile(FILE_PATH + file.getFileName(),"r").getChannel();
				WritableByteChannel outChannel = Channels.newChannel(response.getOutputStream());
		) {
			
			int bufferSize = file.getBufferSize();
			if (bufferSize > channel.size()) {
				bufferSize = (int) channel.size();
			}
			
			log.info("buffer size = {}", bufferSize);
			
			ByteBuffer buffer = ByteBuffer.allocateDirect( bufferSize );
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
	
	public void readWrite (InputStream is, OutputStream os, FileVO file) throws IOException {

	    int bufferSize = file.getBufferSize();
	    int available = is.available();
	    if (bufferSize > available) {
	        bufferSize = available;
	    }
	    
	    log.info("buffer size = {}", bufferSize);
	    
		if(bufferSize > 0) {
			byte[] buffer = new byte[ bufferSize ];
			int read = 0;
			while ( ( read = is.read( buffer, 0, buffer.length ) ) != -1 ) {
				os.write(buffer, 0, read);
			}
			os.flush();
		}
	}
}
