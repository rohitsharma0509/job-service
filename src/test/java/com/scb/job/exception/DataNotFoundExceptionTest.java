package com.scb.job.exception;

import static org.junit.Assert.assertNotNull;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DataNotFoundExceptionTest {
	
	@Mock
    private DataNotFoundException dataNotFoundExceptionUnderTest;

    
    @Test
    @Order(1)
    void dataNotFoundExceptionUnderTest_No_Args() {
        dataNotFoundExceptionUnderTest = new DataNotFoundException();
        assertNotNull(dataNotFoundExceptionUnderTest);
    }
	
    @Test
    @Order(2)
    void dataNotFoundExceptionUnderTest_One_Arg() {
        dataNotFoundExceptionUnderTest = new DataNotFoundException("test");
        assertNotNull(dataNotFoundExceptionUnderTest);
    }
    
    @Test
    @Order(3)
    void dataNotFoundExceptionUnderTest_Two_Args() {
        dataNotFoundExceptionUnderTest = new DataNotFoundException("message", new Exception("test"));
        assertNotNull(dataNotFoundExceptionUnderTest);
    }
    
    @Test
    @Order(4)
    void dataNotFoundExceptionUnderTest_Exeption_Args() {
        dataNotFoundExceptionUnderTest = new DataNotFoundException( new Exception("test"));
        assertNotNull(dataNotFoundExceptionUnderTest);
    }
    
}
