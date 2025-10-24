package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.repositories.AirlineSequenceRepository;

@ExtendWith(MockitoExtension.class)
class FlightCodeServiceTest {

    @Mock
    private AirlineSequenceRepository sequenceRepository;

    private FlightCodeService service;

    @BeforeEach
    void setUp() {
        service = new FlightCodeService(sequenceRepository);
    }

    @Test
    void nextFlightCode_returnsFormattedValue() {
        when(sequenceRepository.nextNumberForPrefix("AF")).thenReturn(7);

        String code = service.nextFlightCode(" af ");

        assertEquals("AF0007", code);
        verify(sequenceRepository).nextNumberForPrefix("AF");
    }

    @Test
    void nextFlightCode_rejectsBlankPrefix() {
        assertThrows(IllegalArgumentException.class, () -> service.nextFlightCode("   "));
        verify(sequenceRepository, never()).nextNumberForPrefix(anyString());
    }

    @Test
    void nextFlightCode_rejectsInvalidPattern() {
        assertThrows(IllegalArgumentException.class, () -> service.nextFlightCode("A*"));
        verify(sequenceRepository, never()).nextNumberForPrefix(anyString());
    }

    @Test
    void nextFlightCode_throwsWhenSequenceMissing() {
        when(sequenceRepository.nextNumberForPrefix("AA")).thenReturn(null);

        assertThrows(IllegalStateException.class, () -> service.nextFlightCode("AA"));
    }
}
