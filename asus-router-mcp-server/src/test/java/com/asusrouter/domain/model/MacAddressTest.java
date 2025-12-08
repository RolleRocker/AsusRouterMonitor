package com.asusrouter.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for MacAddress value object validation and normalization.
 */
class MacAddressTest {
    
    @Test
    void shouldAcceptColonSeparatedMac() {
        MacAddress mac = new MacAddress("AA:BB:CC:DD:EE:FF");
        assertEquals("AA:BB:CC:DD:EE:FF", mac.value());
    }
    
    @Test
    void shouldAcceptHyphenSeparatedMac() {
        MacAddress mac = new MacAddress("AA-BB-CC-DD-EE-FF");
        assertEquals("AA-BB-CC-DD-EE-FF", mac.value());
    }
    
    @Test
    void shouldAcceptLowerCase() {
        MacAddress mac = new MacAddress("aa:bb:cc:dd:ee:ff");
        assertEquals("aa:bb:cc:dd:ee:ff", mac.value());
    }
    
    @Test
    void shouldAcceptMixedCase() {
        MacAddress mac = new MacAddress("Aa:Bb:Cc:Dd:Ee:Ff");
        assertEquals("Aa:Bb:Cc:Dd:Ee:Ff", mac.value());
    }
    
    @Test
    void shouldNormalizeToUpperCaseWithColons() {
        MacAddress mac1 = new MacAddress("aa:bb:cc:dd:ee:ff");
        assertEquals("AA:BB:CC:DD:EE:FF", mac1.normalized());
        
        MacAddress mac2 = new MacAddress("AA-BB-CC-DD-EE-FF");
        assertEquals("AA:BB:CC:DD:EE:FF", mac2.normalized());
        
        MacAddress mac3 = new MacAddress("Aa-Bb-Cc-Dd-Ee-Ff");
        assertEquals("AA:BB:CC:DD:EE:FF", mac3.normalized());
    }
    
    @Test
    void shouldRejectInvalidFormat() {
        assertThrows(IllegalArgumentException.class, () -> 
            new MacAddress("AABBCCDDEEFF"));  // No separators
        
        assertThrows(IllegalArgumentException.class, () -> 
            new MacAddress("AA:BB:CC:DD:EE"));  // Too short
        
        assertThrows(IllegalArgumentException.class, () -> 
            new MacAddress("AA:BB:CC:DD:EE:FF:00"));  // Too long
        
        assertThrows(IllegalArgumentException.class, () -> 
            new MacAddress("ZZ:BB:CC:DD:EE:FF"));  // Invalid hex
    }
    
    @Test
    void shouldRejectNull() {
        assertThrows(IllegalArgumentException.class, () -> 
            new MacAddress(null));
    }
    
    @Test
    void shouldRejectEmpty() {
        assertThrows(IllegalArgumentException.class, () -> 
            new MacAddress(""));
    }
}
