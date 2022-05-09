package com.scb.job.exception;

import static org.junit.Assert.assertNotNull;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ZoneNotFoundExceptionTest {

	@Mock
    private  ZoneNotFoundException  zoneNotFoundExceptionTest;

    @Test
    void zoneNotFoundExceptionTest_One_Arg() {
    	zoneNotFoundExceptionTest = new ZoneNotFoundException("test");
        assertNotNull(zoneNotFoundExceptionTest);
    }
}
