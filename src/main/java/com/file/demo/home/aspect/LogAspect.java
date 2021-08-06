package com.file.demo.home.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
public class LogAspect {
	
	@Around("@annotation(LogExecutionTime)")
	public Object logging( ProceedingJoinPoint jp ) throws Throwable {

		StopWatch stopWatch = new StopWatch("SHS");
		stopWatch.start();
		Object result = jp.proceed();
		stopWatch.stop();

		log.info(stopWatch.prettyPrint());
		log.info("running time = {}", String.valueOf(stopWatch.getTotalTimeSeconds()) + " seconds");
		
		return result;
	}
}
