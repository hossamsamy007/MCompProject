package uk.ac.ncl.burton.twyb.utils;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;

public class BigIntegerUtils {

	public static BigInteger randomBetween(BigInteger lower, BigInteger upper, SecureRandom ran ){
		
		BigInteger x = null;
		
		while( true ){
			x = new BigInteger( upper.bitLength() , ran );
			x = x.mod(upper);
			
			if( x.compareTo(lower) != -1 && x.compareTo(upper) != 1 ) break;
		}
		
		return x;
		
	}
	
	public static BigInteger randomBetween(BigInteger lower, BigInteger upper ){
		
		SecureRandom ran = new SecureRandom();
		return randomBetween(lower,upper,ran);
		
	}
	
	public static BigInteger multiplyBaseExponents( BigInteger mod , List<BigInteger> bases, List<BigInteger> exponents){
		
		if( bases.size() != exponents.size()){
			throw new IllegalArgumentException("The number of bases must match the number of exponents");
		}
		
		BigInteger x = BigInteger.ONE;
		for(int i = 0 ; i < bases.size(); i++ ){
			x = x.multiply( bases.get(i).modPow(exponents.get(i), mod) ).mod(mod);
		}
		return x;
	}

	
	
	public static BigInteger divide( BigInteger x, BigInteger y, BigInteger mod ){
		// x/y = x*y^-1
		
		BigInteger n1 = BigInteger.valueOf(-1);	
		return x.multiply( y.modPow(n1, mod) );
	}
	
	public static BigInteger addList( List<BigInteger> list ){
		BigInteger x = BigInteger.ZERO;
		for( int i = 0 ; i < list.size(); i++ ){
			x = x.add(list.get(i));
		}
		return x;
	}
	
	public static BigInteger multiplyList( List<BigInteger> list ){
		BigInteger x = BigInteger.ONE;
		for( int i = 0 ; i < list.size(); i++ ){
			x = x.multiply(	list.get(i) );
		}
		return x;
	}
	
}
