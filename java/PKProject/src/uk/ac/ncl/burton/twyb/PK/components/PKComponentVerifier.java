package uk.ac.ncl.burton.twyb.PK.components;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

import uk.ac.ncl.burton.twyb.crypto.CyclicGroup;
import uk.ac.ncl.burton.twyb.utils.BigIntegerUtils;

public class PKComponentVerifier {
	
	private UUID component_id;
	public UUID getComponentID() { return component_id; }
	
	
	private CyclicGroup G;
	
	private BigInteger challenge;
	
	private int expectedNumberBases;
	
	public PKComponentVerifier( CyclicGroup G, UUID component_id , int expectedNumberBases){
		this.G = G;
		this.component_id = component_id;
		this.expectedNumberBases = expectedNumberBases;
		generateRandomValues();
	}
	
	private void generateRandomValues(){
		challenge = BigIntegerUtils.randomBetween( BigInteger.ONE, G.getQ() );
	}
	
	public BigInteger getChallenge(){
		return challenge;
	}
	
	public void setChallenge( BigInteger c){
		challenge = c;
	}

	public boolean verify( List<BigInteger> bases, List<BigInteger> responses, BigInteger commitment, BigInteger value ){
		//u^s1 g^s2 == t(x)^c
		
		if( bases.size() != expectedNumberBases ) throw new IllegalArgumentException("The number of bases does not match expected. (Received " + bases.size()+ " Expected " + expectedNumberBases+ ")");
		if( bases.size() != responses.size() ) throw new IllegalArgumentException("The number of bases must match the number of responses");
		
		BigInteger leftSide = BigIntegerUtils.multiplyBaseExponents(G.getQ(), bases, responses);
		BigInteger rightSide = commitment.multiply( value.modPow(challenge, G.getQ()) ).mod(G.getQ());
		
		return leftSide.equals(rightSide);
		
	}

	
	
	
	
}
