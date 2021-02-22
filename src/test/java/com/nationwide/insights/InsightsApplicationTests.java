package com.nationwide.insights;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;


@SpringBootTest
@TestPropertySource(locations = "classpath:application-local.properties")
@ActiveProfiles("test")
class InsightsApplicationTests {

	@Test
	void contextLoads() {
	}

}
