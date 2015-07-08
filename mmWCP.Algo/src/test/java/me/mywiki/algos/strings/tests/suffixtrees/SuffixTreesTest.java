package me.mywiki.algos.strings.tests.suffixtrees;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;

import me.mywiki.algos.strings.SuffixTreeAlgorithms;
import me.mywiki.algos.strings.SuffixTreeAlgorithms.SuffixTree;
import me.mywiki.algos.strings.impl.suffixtrees.UkkonenConstructionOptimized0;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class SuffixTreesTest {

    @Parameters(name="{0}")
    public static Collection<Object[]> testData() {

        Collection<Object[]> result= new ArrayList<>( toBidimensionalByRow( "",
                                                    "abbbbb",
                                                    "b",
                                                    "ab",
                                                    "aa",
                                                    "abc",
                                                    "abcd",
                                                    "abcdef",
                                                    "aaaaaaa",
                                                    
                                                    "baa",
                                                    "aaab",
                                                    "abbb",
                                                    "aba",
                                                    "abab",
                                                    "ababab",
                                                    "ababc",
                                                    "abababx",/**/
                                                    
                                                    "abcaaaaabcabc",
                                                    "mabcabcabcabc",
                                                    "mississipi",/**/
                                                    "mississiabsiabab",//b"//ab"//ababc"/**/
                                                    "mississiabsiababb"

        ));
        result.addAll(createTwoVeryLongStrings());
        return result;
    }

    private static Collection<Object[]> createTwoVeryLongStrings() {
        int logLen= 12; 
        Object []firstRandom= { createHardString(logLen) };
        Object [] second= { new StringBuilder((1<<logLen)<<1)
                            .append(firstRandom[0])
                            .append(firstRandom[0])
                            .toString() };
        return Arrays.<Object[]>asList(firstRandom, second);
    }

    private static String createHardString(int logLen) {
        Random rand= new Random();
        StringBuilder sb= new StringBuilder(1<<logLen);
        sb.append((char)('a'+ rand.nextInt(26)));
        for (int i=0; i< logLen; i++) {
            sb.append(sb.toString()).append((char)('a'+rand.nextInt(26)));
        }
        return sb.reverse().toString();
    }

    @Parameter
    public String stringUnderTest;
   
    @Test
    public void testSuffixTreeConstructionOptimized() {
        String testName= stringUnderTest.length()< 50 
                             ? stringUnderTest
                             : stringUnderTest.substring(0, 50) + " ... ["+stringUnderTest.length() +" characters] ";
        System.out.println("testing suffix free for: " + testName);
        long start= System.currentTimeMillis();
        SuffixTree sTree= UkkonenConstructionOptimized0.construct( stringUnderTest );
        long end= System.currentTimeMillis();
        System.err.println("Suffix tree constructed in "+(end-start) +"ms.");
        SuffixTreeAlgorithms.verifySuffixTree(sTree, stringUnderTest);
    }
    
    private static Collection<Object[]> toBidimensionalByRow(Object ... args) {
        Object[][] result= new Object[args.length][];
        for (int i=0; i<args.length;i++) {
            result[i]= new Object[]{args[i]};
        }
        return Arrays.asList(result);
    }
    
}
