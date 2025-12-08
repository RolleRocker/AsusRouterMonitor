package com.asusrouter.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for IpAddress value object validation.
 */
class IpAddressTest {
    
    @Test
    void shouldAcceptValidIpAddress() {
        IpAddress ip = new IpAddress("192.168.1.1");
        assertEquals("192.168.1.1", ip.value());
    }
    
    @Test
    void shouldAcceptZeros() {
        IpAddress ip = new IpAddress("0.0.0.0");
        assertEquals("0.0.0.0", ip.value());
    }
    
    @Test
    void shouldAcceptMaxValues() {
        IpAddress ip = new IpAddress("255.255.255.255");
        assertEquals("255.255.255.255", ip.value());
    }
    
    @Test
    void shouldRejectInvalidFormat() {
        assertThrows(IllegalArgumentException.class, () -> 
            new IpAddress("192.168.1"));
        
        assertThrows(IllegalArgumentException.class, () -> 
            new IpAddress("192.168.1.1.1"));
        
        assertThrows(IllegalArgumentException.class, () -> 
            new IpAddress("abc.def.ghi.jkl"));
    }
    
    @Test
    void shouldRejectOutOfRangeOctets() {
        assertThrows(IllegalArgumentException.class, () -> 
            new IpAddress("256.1.1.1"));
        
        assertThrows(IllegalArgumentException.class, () -> 
            new IpAddress("1.256.1.1"));
        
        assertThrows(IllegalArgumentException.class, () -> 
            new IpAddress("1.1.256.1"));
        
        assertThrows(IllegalArgumentException.class, () -> 
            new IpAddress("1.1.1.256"));
        
        assertThrows(IllegalArgumentException.class, () -> 
            new IpAddress("1.1.1.-1"));
    }
    
    @Test
    void shouldRejectNull() {
        assertThrows(IllegalArgumentException.class, () -> 
            new IpAddress(null));
    }
    
    @Test
    void shouldRejectEmpty() {
        assertThrows(IllegalArgumentException.class, () -> 
            new IpAddress(""));
    }
}
