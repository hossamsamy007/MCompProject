package uk.ac.ncl.burton.twyb.crypto;

import java.math.BigInteger;

/**
 * Extended Euclidian Algorithm
 * @author twyburton
 *
 */
public class EEA {

	public static BigInteger gcd( BigInteger x , BigInteger y ){
		BigInteger out = x.mod(y);
		if( !out.equals(BigInteger.ZERO) ){
			return gcd( y, out );
		}
		return y;
	}
	
	public static EEAResult eea( BigInteger x , BigInteger y ){
				
		if( !y.equals(BigInteger.ZERO) ){
			// Recursive case
			EEAResult r = eea( y, x.mod(y) );
			return new EEAResult( r.getD(), x , r.getT() , y, r.getS().subtract( (r.getT()).multiply(x.divide(y)) ) );
		}

		// Base case
		return new EEAResult( x, x, BigInteger.ONE, y, BigInteger.ZERO );
	}
	
}
