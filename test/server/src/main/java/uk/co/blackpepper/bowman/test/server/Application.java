/*
 * Copyright 2016 Black Pepper Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.blackpepper.bowman.test.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		new Application().run(args);
	}

	void run(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public ObjectMapper getObjectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setVisibility(objectMapper.getSerializationConfig()
			.getDefaultVisibilityChecker()
			.withFieldVisibility(Visibility.ANY)
			.withGetterVisibility(Visibility.NONE)
			.withIsGetterVisibility(Visibility.NONE));
		return objectMapper;
	}
}
