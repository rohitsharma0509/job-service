package com.scb.job.kafka;

import com.scb.job.kafka.consumer.CustomCooperativeStickyAssignor;
import static org.junit.Assert.assertNotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CustomCooperativeStickyAssignorTest {

    @InjectMocks
    private CustomCooperativeStickyAssignor customCooperativeStickyAssignor;

    @Test
    public void supportedProtocolsTest(){
        assertNotNull(customCooperativeStickyAssignor.supportedProtocols());
    }
}
