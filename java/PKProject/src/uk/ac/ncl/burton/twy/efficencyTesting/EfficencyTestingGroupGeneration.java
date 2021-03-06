package uk.ac.ncl.burton.twy.efficencyTesting;

import static org.junit.Assert.*;

import java.math.BigInteger;

import org.junit.Test;

import uk.ac.ncl.burton.twy.crypto.CyclicGroup;

public class EfficencyTestingGroupGeneration {

	//@Test
	public void groupGenerationTest() {
		
		int[] bitLength = {128,256,512,1024};
		int numberRuns = 20;
		
		for( int b = 0 ; b < bitLength.length; b++ ){
			for( int n = 0; n < numberRuns; n++ ){
				
				long startT = System.currentTimeMillis();
				CyclicGroup G =  CyclicGroup.generateGroup(bitLength[b]);
				long endT = System.currentTimeMillis();
				long deltaT = endT - startT;
				
				System.out.println( bitLength[b] + "\t" + n + "\t" + deltaT );
				
			}
		}
		
		
	}
	
	@Test
	public void generateSingleGroup(){
		CyclicGroup G =  CyclicGroup.generateGroup(2048);
		
		System.out.println( "new CyclicGroup(\"" + G.getP() + "\"\n,\"" + G.getQ() + "\"\n,\"" +  G.getG()+ "\");" );
		
		CyclicGroup x = new CyclicGroup("165235115736443943588818400147380723923618267196408211699462556291058562454979"
				,"82617557868221971794409200073690361961809133598204105849731278145529281227489"
				,"154448819522072544007676540804572917254496788104505311455930982037639126117017");
		
	}

}
