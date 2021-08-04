package com.file.demo.home;

import com.file.demo.home.enm.FileDownLoadEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class FileVO {
	private String fileName;
	private FileDownLoadEnum type;
}
