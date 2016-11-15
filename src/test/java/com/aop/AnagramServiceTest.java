package com.aop;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Repeat;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AnagramServiceTest {

    @Test
    public void testHandleWord() throws Exception {
        AnagramService service = new AnagramService();
        service.handleWord("act");
        service.handleWord("cat");
        service.handleWord("tree");
        service.handleWord("race");
        service.handleWord("care");
        service.handleWord("acre");
        service.handleWord("bee");

        List<String> anagrams = AnagramService.reduceAnagrams(service.getMap());

        assertTrue(anagrams.contains("act cat"));
        assertTrue(anagrams.contains("race care acre"));
    }
}