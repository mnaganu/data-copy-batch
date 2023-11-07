package com.example.mnaganu.dcb;

import com.example.mnaganu.dcb.domain.service.DataCopyService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class DataCopyBatchApplication {

	public static void main(String[] args) {
		try (ConfigurableApplicationContext context =
				SpringApplication.run(DataCopyBatchApplication.class, args)) {
			DataCopyService dataCopyService = context.getBean(DataCopyService.class);
			dataCopyService.copy();
		}
	}

}
