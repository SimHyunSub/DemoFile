package com.file.demo.home;

import javax.servlet.http.HttpServletResponse;

public interface HomeService {
	public void fileHandler(HttpServletResponse response, FileVO vo);
}
