package com.example.devops_project;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

class DevopsProjectApplicationTests {

	@Test
	void main_ShouldCallSpringApplicationRun() {

		String[] args = {};


		try (MockedStatic<SpringApplication> springApplicationMock = mockStatic(SpringApplication.class)) {
			springApplicationMock.when(() -> SpringApplication.run(DevopsProjectApplication.class, args))
					.thenReturn(null);

			DevopsProjectApplication.main(args);

			springApplicationMock.verify(() -> SpringApplication.run(DevopsProjectApplication.class, args), times(1));
		}
	}
}
