package com.vitacheck;

import com.amazonaws.services.s3.AmazonS3;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class VitacheckBeApplicationTests {

    @MockBean
    private AmazonS3 amazonS3;

    @Test
    void contextLoads() {
    }

}
