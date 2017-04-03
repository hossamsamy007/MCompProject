package uk.ac.ncl.burton.twyb.equalityTestingImplementation;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import uk.ac.ncl.burton.twyb.PK.PKProver;
import uk.ac.ncl.burton.twyb.PK.PKVerifier;
import uk.ac.ncl.burton.twyb.PK.components.PKComponentProver;
import uk.ac.ncl.burton.twyb.PK.network.NetworkConnectionClient;
import uk.ac.ncl.burton.twyb.crypto.CyclicGroup;
import uk.ac.ncl.burton.twyb.crypto.EEA;
import uk.ac.ncl.burton.twyb.crypto.EEAResult;
import uk.ac.ncl.burton.twyb.utils.BigIntegerUtils;

public class PartyClient {

	public static void main(String[] args) {
		
		
		NetworkConnectionClient client = new NetworkConnectionClient("localhost");
		Thread clientThread = new Thread(client);
		clientThread.start();
		
		client.setBlockingMode(true);
		
		
		// ====== PKS ======
		SecureRandom ran = new SecureRandom();
		
		CyclicGroup G = CyclicGroup.generateGroup(256);
		BigInteger g = G.getG();
		
		BigInteger a = BigInteger.TEN;
		
		BigInteger mod = G.getQ();
		
		// === STEP 1 ===
		
		// -- Calculations --
		BigInteger h =  G.generateGenerator();
		BigInteger x1 = RandomZQ(g,G.getQ(),ran);//BigIntegerUtils.randomBetween(BigInteger.ONE, G.getQ(), ran); 
		BigInteger x2 = RandomZQ(g,G.getQ(),ran);//BigIntegerUtils.randomBetween(BigInteger.ONE, G.getQ(), ran);
		BigInteger r =  RandomZQ(g,G.getQ(),ran);//BigIntegerUtils.randomBetween(BigInteger.ONE, G.getQ(), ran);
		
		BigInteger c =  g.modPow( x1, mod ).multiply( h.modPow( x2, mod));
		BigInteger u1 = g.modPow(r, mod );
		BigInteger u2 = h.modPow(r, mod );
		BigInteger e =  g.modPow( a, mod ).multiply( c.modPow( r, mod));
		
		// -- PoK --
		PKProver peggy = new PKProver(G);
		//PKVerifier victor = new PKVerifier(G,3);
		
		// (1)
		List<BigInteger> bases = new ArrayList<BigInteger>();
		List<BigInteger> exponents = new ArrayList<BigInteger>();
		bases.add(g);
		exponents.add(r);
		PKComponentProver c1X1 = PKComponentProver.generateProver(G, bases, exponents);
		
		// (2)
		bases = new ArrayList<BigInteger>();
		exponents = new ArrayList<BigInteger>();
		bases.add(h);
		exponents.add(r);
		PKComponentProver c1X2 = PKComponentProver.generateProver(G, bases, exponents);
		
		// (3)
		bases = new ArrayList<BigInteger>();
		exponents = new ArrayList<BigInteger>();
		bases.add(g);
		exponents.add(a);
		bases.add(c);
		exponents.add(r);
		PKComponentProver c1X3 = PKComponentProver.generateProver(G, bases, exponents);
		
		// -- ADD --
		peggy.addComponent(c1X1);
		peggy.addComponent(c1X2);
		peggy.addComponent(c1X3);
		
		// -- Proof --
		String init = peggy.getJSONInitialise();
		client.sendMessage(init);
		
		String commitment = peggy.getJSONCommitment();
		client.sendMessage(commitment);
		
		String challenge = client.receiveMessage();
		
		String response = peggy.getJSONResponse(challenge);
		client.sendMessage(response);
		
		String passing = peggy.getJSONPassingVariables();
		client.sendMessage(passing);
		
		String outcome = client.receiveMessage();
		System.out.println("<CLIENT> " + outcome);
		
		
		
		
		// === STEP 2 ===
		init = client.receiveMessage();
		PKVerifier victor = PKVerifier.getInstance(init);

		commitment = client.receiveMessage();
		
		challenge = victor.getJSONChallenge(commitment);
		client.sendMessage(challenge);
		
		response = client.receiveMessage();
		passing = client.receiveMessage();
		
		outcome = victor.getJSONOutcome(commitment, response, passing);
		client.sendMessage(outcome);
		
		boolean success = victor.isProofSucessful();
		System.out.println(success);
		
		
		BigInteger u1d = victor.getValue(0);
		BigInteger u2d = victor.getValue(1);
		BigInteger ed = victor.getValue(2);
		
		// === STEP 3 ===
		
		// -- Calculations --
		BigInteger z = RandomZQ(g,G.getQ(),ran);//BigIntegerUtils.randomBetween(BigInteger.ONE, G.getQ(), ran);
		
		
		BigInteger edz = ed.modPow(z, mod);
			BigInteger zx1 = z.multiply(x1);
		BigInteger u1dzx1 = u1d.modPow( zx1 , mod).modInverse(mod);
			BigInteger zx2 = z.multiply(x2);
		BigInteger u2dzx2 = u2d.modPow( zx2 , mod).modInverse(mod);
	
		BigInteger d = edz.multiply(u1dzx1).multiply(u2dzx2).mod(mod);
		
		// GCD PROOF:   PK{(z,q,r1,r2,a,b) : C = g^z h^r1 AND D = g^q h^r2 AND g = C^a D^b
		BigInteger r1 = RandomZQ(g,G.getQ(),ran);
		BigInteger r2 = RandomZQ(g,G.getQ(),ran);
		
		EEAResult eea = EEA.eea(z, G.getQ());
		
		List<BigInteger> baseseea = new ArrayList<BigInteger>();
		List<BigInteger> exponentseea = new ArrayList<BigInteger>();
		baseseea.add(g);
		baseseea.add(h);
		exponentseea.add(z);
		exponentseea.add(r1);
		BigInteger C = BigIntegerUtils.multiplyBaseExponents(mod, baseseea, exponentseea);
		
		baseseea = new ArrayList<BigInteger>();
		exponentseea = new ArrayList<BigInteger>();
		baseseea.add(g);
		baseseea.add(h);
		exponentseea.add(mod);
		exponentseea.add(r2);
		BigInteger D = BigIntegerUtils.multiplyBaseExponents(mod, baseseea, exponentseea);
	
		// -- PoK --
		peggy = new PKProver(G);
		
		// (1)
		bases = new ArrayList<BigInteger>();
		exponents = new ArrayList<BigInteger>();
		bases.add(g);
		exponents.add(x1);
		bases.add(h);
		exponents.add(x2);
		PKComponentProver c3X1 = PKComponentProver .generateProver(G, bases, exponents);
		
		// (2)
		bases = new ArrayList<BigInteger>();
		exponents = new ArrayList<BigInteger>();
		bases.add(ed);
		exponents.add(z);
		bases.add(u1d);
			List<BigInteger> nzx1List = new ArrayList<BigInteger>(); nzx1List.add(BigInteger.valueOf(-1)); nzx1List.add(z); nzx1List.add(x1);
		exponents.add(BigIntegerUtils.multiplyList(nzx1List));
		bases.add(u2d);
			List<BigInteger> nzx2List = new ArrayList<BigInteger>(); nzx2List.add(BigInteger.valueOf(-1)); nzx2List.add(z); nzx2List.add(x2);
		exponents.add(BigIntegerUtils.multiplyList(nzx2List));
		PKComponentProver c3X2 = PKComponentProver .generateProver(G, bases, exponents);
		
		// (3)
		bases = new ArrayList<BigInteger>();
		exponents = new ArrayList<BigInteger>();
		bases.add(g);
		exponents.add(z);
		bases.add(h);
		exponents.add(r1);
		PKComponentProver c3X3 = PKComponentProver.generateProver(G, bases, exponents);
		
		// (4)
		bases = new ArrayList<BigInteger>();
		exponents = new ArrayList<BigInteger>();
		bases.add(g);
		exponents.add(mod);
		bases.add(h);
		exponents.add(r2);
		PKComponentProver c3X4 = PKComponentProver.generateProver(G, bases, exponents);
		
		// (5)
		bases = new ArrayList<BigInteger>();
		exponents = new ArrayList<BigInteger>();
		bases.add(C);
		exponents.add(eea.getS());
		bases.add(D);
		exponents.add(eea.getT());
		PKComponentProver c3X5 = PKComponentProver.generateProver(G, bases, exponents);
		
		
		// -- ADD --
		peggy.addComponent(c3X1);
		peggy.addComponent(c3X2);
		peggy.addComponent(c3X3);
		peggy.addComponent(c3X4);
		peggy.addComponent(c3X5);
		
		// -- Proof --
		init = peggy.getJSONInitialise();
		client.sendMessage(init);
		
		commitment = peggy.getJSONCommitment();
		client.sendMessage(commitment);
		
		challenge = client.receiveMessage();
		
		response = peggy.getJSONResponse(challenge);
		client.sendMessage(response);
		
		passing = peggy.getJSONPassingVariables();
		client.sendMessage(passing);
		
		outcome = client.receiveMessage();
		System.out.println(outcome);
		
		System.out.println("d: " + d );
		
		
	/*	
		// === STEP 2 ===
		
		// -- Calculations --
		BigInteger s = RandomZQ(g,G.getQ(),ran);//BigIntegerUtils.randomBetween(BigInteger.ONE, G.getQ(), ran);
		BigInteger t = RandomZQ(g,G.getQ(),ran);//BigIntegerUtils.randomBetween(BigInteger.ONE, G.getQ(), ran);
		
		
		BigInteger u1d = u1.modPow(s, mod).multiply( g.modPow(t, mod) );
		BigInteger u2d = u2.modPow(s, mod).multiply( h.modPow(t, mod) );
		
			BigInteger es = e.modPow(s, mod);
				BigInteger bs = b.multiply(s);
			BigInteger gbs = g.modPow( bs ,mod).modInverse(mod);
			BigInteger ct = c.modPow(t, mod);
		
		BigInteger ed = es.multiply(gbs).multiply(ct);	
		
		
		// -- PoK --
		peggy = new PKProver(G);
		//victor = new PKVerifier(G,3);
		
		// (1)
		bases = new ArrayList<BigInteger>();
		exponents = new ArrayList<BigInteger>();
		bases.add(u1);
		exponents.add(s);
		bases.add(g);
		exponents.add(t);
		PKComponentProver c2X1 = PKComponentProver .generateProver(G, bases, exponents);
		
		// (2)
		bases = new ArrayList<BigInteger>();
		exponents = new ArrayList<BigInteger>();
		bases.add(u2);
		exponents.add(s);
		bases.add(h);
		exponents.add(t);
		PKComponentProver c2X2 = PKComponentProver .generateProver(G, bases, exponents);
		
		// (3)
		bases = new ArrayList<BigInteger>();
		exponents = new ArrayList<BigInteger>();
		bases.add(e);
		exponents.add(s);
		bases.add(g);
			List<BigInteger> nbsList = new ArrayList<BigInteger>(); nbsList.add(BigInteger.valueOf(-1)); nbsList.add(b); nbsList.add(s);
		exponents.add(BigIntegerUtils.multiplyList(nbsList));
		bases.add(c);
		exponents.add(t);
		PKComponentProver c2X3 = PKComponentProver .generateProver(G, bases, exponents);
		
		// -- ADD --
		peggy.addComponent(c2X1);
		peggy.addComponent(c2X2);
		peggy.addComponent(c2X3);
		
		// <<<<<<<<<<<<<<<<<<<<<<<< NEED TO DO GCD
		
		// -- Proof --
		init = peggy.getJSONInitialise();
		System.out.println(init);
		victor = PKVerifier.getInstance(init);
		
		commitment = peggy.getJSONCommitment();
		System.out.println(commitment);
		
		challenge = victor.getJSONChallenge(commitment);
		System.out.println(challenge);
		
		response = peggy.getJSONResponse(challenge);
		System.out.println(response);
		
		passing = peggy.getJSONPassingVariables();
		System.out.println(passing);
		
		outcome = victor.getJSONOutcome(commitment, response, passing);
		System.out.println(outcome);
		
		// === STEP 3 ===
		
		// -- Calculations --
		BigInteger z = RandomZQ(g,G.getQ(),ran);//BigIntegerUtils.randomBetween(BigInteger.ONE, G.getQ(), ran);
		
		
		BigInteger edz = ed.modPow(z, mod);
			BigInteger zx1 = z.multiply(x1);
		BigInteger u1dzx1 = u1d.modPow( zx1 , mod).modInverse(mod);
			BigInteger zx2 = z.multiply(x2);
		BigInteger u2dzx2 = u2d.modPow( zx2 , mod).modInverse(mod);
	
		BigInteger d = edz.multiply(u1dzx1).multiply(u2dzx2).mod(mod);
	
		// -- PoK --
		peggy = new PKProver(G);
		//victor = new PKVerifier(G,2);
		
		// (1)
		bases = new ArrayList<BigInteger>();
		exponents = new ArrayList<BigInteger>();
		bases.add(g);
		exponents.add(x1);
		bases.add(h);
		exponents.add(x2);
		PKComponentProver c3X1 = PKComponentProver .generateProver(G, bases, exponents);
		
		// (2)
		bases = new ArrayList<BigInteger>();
		exponents = new ArrayList<BigInteger>();
		bases.add(ed);
		exponents.add(z);
		bases.add(u1d);
			List<BigInteger> nzx1List = new ArrayList<BigInteger>(); nzx1List.add(BigInteger.valueOf(-1)); nzx1List.add(z); nzx1List.add(x1);
		exponents.add(BigIntegerUtils.multiplyList(nzx1List));
		bases.add(u2d);
			List<BigInteger> nzx2List = new ArrayList<BigInteger>(); nzx2List.add(BigInteger.valueOf(-1)); nzx2List.add(z); nzx2List.add(x2);
		exponents.add(BigIntegerUtils.multiplyList(nzx2List));
		PKComponentProver c3X2 = PKComponentProver .generateProver(G, bases, exponents);
		
		
		// -- ADD --
		peggy.addComponent(c3X1);
		peggy.addComponent(c3X2);
		
		// <<<<<<<<<<<<<<<<<<<<<<<< NEED TO DO GCD
		
		// -- Proof --
		init = peggy.getJSONInitialise();
		System.out.println(init);
		victor = PKVerifier.getInstance(init);
		
		commitment = peggy.getJSONCommitment();
		System.out.println(commitment);
		
		challenge = victor.getJSONChallenge(commitment);
		System.out.println(challenge);
		
		response = peggy.getJSONResponse(challenge);
		System.out.println(response);
		
		passing = peggy.getJSONPassingVariables();
		System.out.println(passing);
		
		outcome = victor.getJSONOutcome(commitment, response, passing);
		System.out.println(outcome);
		
		
		
		
		*/
		
		
		
		
		
		
		
		
		
		/*while( !client.isConnectionClosed() ){
			try {
				Thread.sleep(10);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		}*/
		

	}
	
	
	private static BigInteger RandomZQ(BigInteger g, BigInteger q, SecureRandom ran){
		return BigIntegerUtils.randomBetween(BigInteger.ONE, q, ran);
	}

	
}
