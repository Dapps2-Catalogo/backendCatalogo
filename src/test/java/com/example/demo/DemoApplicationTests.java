package com.example.demo;
// prueba 
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import com.example.demo.repositories.VueloRepository;
import com.example.demo.service.VueloService;

@SpringBootTest
@ActiveProfiles("test")
class DemoApplicationTests {

	@SuppressWarnings("removal")
	@MockBean
	private VueloService vueloService;

	@SuppressWarnings("removal")
	@MockBean
	private VueloRepository vueloRepository;

	@Test
	void contextLoads() {
	}

}
