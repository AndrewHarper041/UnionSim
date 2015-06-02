//Andrew Harper adh41
import java.util.*;
import java.io.*;


public class UnionSim
{
	public enum Type {PIZZA, STRUTTERS, SALAD, SPECIAL, TACO, DRINK};
	public enum State {NOTARRIVED, BOUGHT, CHECKOUT, DRINK};
	
	static PriorityQueue<Person> eventQueue = new PriorityQueue<Person>();
	static LinkedList<Person> allPeople = new LinkedList<Person>();
	static Random rand = new Random();
	static double time = 0; //Time described in seconds
	static int income = 0; //Money made, cumlitive. Increased when Customer arrives, decreased when one gets refund
	static int expense = 0; //Money spent, increased when plane leaves and increased by 25 per hour for checkIn station in use 
	static int profit = 0;
	static int days;
	static int dayCount = 0;
	static int peopleAte = 0;
	static double checkTime = .224;
	static double customerRate = 0;
	static double drinkTime = .113;
	static ArrayList<Eatery> eaterys = new ArrayList<Eatery>();
	static ArrayList<Cashier> newCash = new ArrayList<Cashier>();
	static OldCashier oldCashier = new OldCashier();
	static boolean oldCheckout = false;
    static double multVenProb = 0.0;
	static PrintWriter wr;
	
	static double avgTaco = 0;
	static double numTaco = 0;
	static double avgPizza = 0;
	static double numPizza = 0;
	static double avgSalad = 0;
	static double numSalad = 0;
	static double avgSpecial = 0;
	static double numSpecial = 0;
	static double avgStrutters = 0;
	static double numStrutters = 0;
	static double numComposite = 0;
	static double avgComposite = 0;
	static double totalAvg = 0;


	//Generate all objects needed for simulation

	/*static Eatery pizzaHut = new Eatery(Type.PIZZA, 37800); //0 - 37800 (10.5 hours)
	static Eatery special = new Eatery(Type.SPECIAL, 34200); //0 - 34200 (9.5 hours)
	static Eatery tacoBell = new Eatery(Type.TACO, 37800); //0 - 37800 (10.5 hours)
	static Eatery salad = new Eatery(Type.SALAD, 32400); //0 - 32400 (9 hours)
	static Eatery strutters = new Eatery(Type.STRUTTERS, 34200); //0 - 34200 (9.5 hours)
	static Eatery drink = new Eatery(Type.DRINK, 37800);*/
	
	
	
	public static void main(String args[])
	{
		
		try
		{
		wr = new PrintWriter(new File(args[0]));
		wr.print(" <html>" + "\n" + "<head>" + "\n" + "<link href=\"css/bootstrap.css\" rel=\"stylesheet\" type=\"text/css\">" + "\n" + "<link href=\"css/style.css\" rel=\"stylesheet\" type=\"text/css\" />" + "\n" + "</head>" + "\n" + "<body>" + "\n");
		

		eaterys.add(new Eatery(Type.PIZZA, 37800, 1));
		eaterys.add(new Eatery(Type.SPECIAL, 34200, 2));
		eaterys.add(new Eatery(Type.TACO, 37800, 3));
		eaterys.add(new Eatery(Type.SALAD, 32400, 4));
		eaterys.add(new Eatery(Type.STRUTTERS, 34200, 2));
		eaterys.add(new Eatery(Type.DRINK, 37800, 5));
		
		newCash.add(new Cashier(1));
		newCash.add(new Cashier(2));
		newCash.add(new Cashier(3));
		newCash.add(new Cashier(4));
		newCash.add(new Cashier(5));
		
		//Get user input on # of days
		String input = "";
		Scanner sc = new Scanner(System.in);
		System.out.print("Input the probability of the number of vendors the customer will visit (Probably that they will go to multiple vendors)");
		multVenProb = sc.nextDouble();
		
		if(multVenProb == 0)
			System.out.println("i quit");
		oldCheckout = true;

		Person per;
		
		for(int two = 0; two < 2; two++)
		{	
			hereBeJank(two);
			
			for(int cRate = 0; cRate < 8; cRate++)
			{
				//Make a customerRate to be used
				customerRate = getCusRate(cRate);

				generateCustomer(customerRate);

				System.out.println(customerRate);
				System.out.println(eventQueue.size());
							
				int numInter;
				while(!eventQueue.isEmpty())
				{

					per = eventQueue.poll();
						
					time = per.time;
										
					handleEvent(per);
					
					/*if(!oldCheckout)
					{
					for(Eatery e : eaterys)
					{
						
						System.out.println(e.type + " line: " + e.line.size());
						System.out.println(e.type + " cashier: " + e.cashier.line.size());
						
					}
					}*/
				}				

				
				getData();
				

				wr.println("<tbody>");
				wr.println("<tr>");
				wr.println("<td>" + customerRate + "</td>");
				wr.println("<td>" + avgTaco/numTaco + "</td>");
				wr.println("<td>" + avgPizza/numPizza + "</td>");
				wr.println("<td>" + avgSalad/numSalad + "</td>");
				wr.println("<td>" + avgSpecial/numSpecial + "</td>");
				wr.println("<td>" + avgStrutters/numStrutters + "</td>");
				if(multVenProb == 0)
					wr.println("<td>" + avgComposite/numComposite + "</td>");
				wr.println("<td>" + totalAvg/allPeople.size() + "</td>");
				wr.println("</tr>");
				
				allPeople.clear();
				resetCounts();
			}
			
			wr.print("</tbody>" + "\n" + "</table>" + "\n" + "</div>");
		}
		wr.print("\n" + "</body>" + "\n" + "</html>");
		wr.flush();
		}catch(Exception e){System.out.println(e);}
			
	}
		
	
	//-----------------------------------------------FUNCTIONS---------------------------------------------------------
	
	public static void handleEvent(Person per)
	{
		//System.out.println("HANDLE ME");
                
		//Normal customer events
		if(oldCheckout)
		{
			switch(per.state)
			{
				case NOTARRIVED: //People arrive, and are processed or placed in line
					for(Eatery e : eaterys)
						if(per.type == e.type)
							e.arrive(per);

					break;
					
				case BOUGHT: //People leave food place, and sent to checkout
					for(Eatery e : eaterys)
						if(per.type == e.type)
							e.popLine(per);
							
					if(per.numVendersDesired > per.numVendorsVisited)
					{
						//call the method TakYee Wrote control f elevator
						//This method is going to check where they've been and replace it with a new one
					   vendorChecker(per);
					   break;
					}
						
					else
					{
						if(!per.drink)
							oldCashier.arrive(per);
						
						else if(per.drink)
						{
							for(Eatery e : eaterys)
								if(e.type == Type.DRINK)
									e.arrive(per);
						}
					}
						
					break;
					
				case DRINK:
					for(Eatery e : eaterys)
						if(e.type == Type.DRINK)
						{
							e.popLine(per);
						}
					
					oldCashier.arrive(per);
					break; 
					
				case CHECKOUT: //People leave food place, POPING the line, and being placed in Drink process if oldCheckout.
					oldCashier.popLine(per);
					allPeople.add(per);
					break;
			}
		}
		
		else if(!oldCheckout)
		{
			switch(per.state)
			{
				case NOTARRIVED: //People arrive, and are processed or placed in line
					for(Eatery e : eaterys)
						if(per.type == e.type)
							e.arrive(per);
					break;
					
				case BOUGHT: //People leave food place, POPING the line, and sent to checkout
				    for(Eatery e : eaterys)
						if(per.type == e.type)
							e.popLine(per);
                       
					if(per.numVendersDesired > per.numVendorsVisited)
					{
						//call the method TakYee Wrote control f elevator
						//This method is going to check where they've been and replace it with a new one
					   vendorChecker(per);
					   break;
					}
					
					else
					{
						for(Eatery e : eaterys)
							if(per.type == e.type)
							{
								for(Cashier c : newCash)
									if(c.name == e.cashName)
										c.arrive(per);
							}
					}
					
					break;
					
				case CHECKOUT: //People leave food place, POPING the line, and being placed in Drink process if oldCheckout.
					for(Eatery e : eaterys)
						if(per.type == e.type)
						{
							for(Cashier c : newCash)
								if(c.name == e.cashName)
									c.popLine(per);
						}
					
					allPeople.add(per);
					break;
			}
		}
	}

	
	//Generates a days customers, with arrival time and where they want to eat
	//Change lambda based on time of day for accurate distribution
	public static void generateCustomer(double lambda)
	{
		Person tempPer;
		double aTime = 0;
		double bags;
		int s = eventQueue.size();
		while(aTime <= 3600000)
		{
			aTime += (-Math.log(1.0 - rand.nextDouble()) / lambda) * 60;//Generate time for next arrival * 60 to convert to sec
			int tempNumVendors = 1; //this used to be 0
			boolean enoughVendors = false;
			while(enoughVendors == false && tempNumVendors < 5)
			{
				double a=rand.nextDouble();
				if(a < multVenProb)
					tempNumVendors++;
				else
					enoughVendors=true;
			}
			//System.out.println("tempNumVndors"+tempNumVendors);
			//************************************THIS IS WHERE TAKYEE ADDED TEMPTYPE INTO THE ARRAYLIST****************************
			Type tempType = getType();
			tempPer = new Person(aTime, tempType, getDrink(), tempNumVendors, 0);
			//tempPer.prevVen.add(tempType);
			eventQueue.add(tempPer);
			tempPer.prevVen.add(tempPer.type);//adds type
		}
	}
	
	public static Type getType()
	{
		Type ty = Type.PIZZA;
		double temp = rand.nextDouble();
		if(temp > 0 && temp <= .443)
			ty = Type.PIZZA;
		if(temp > .443 && temp <= .713)
			ty = Type.TACO;
		if(temp > .713&& temp <= .779)
			ty = Type.SALAD;
		if(temp > .779 && temp <= .820)
			ty = Type.SPECIAL;
		if(temp > .820 && temp <= 1)
			ty = Type.STRUTTERS;
		
		
		return ty;
	}
	
	public static boolean getDrink()
	{
		Type ty;
		double temp = rand.nextDouble();
		if(temp > .5)
			return true;
		else if(temp <= .5)
			return false;
			
		System.out.println("error");
		return false;
	}
	
	
	//------------------------------------------------OBJECTS-----------------------------------------------------------
	
	//Customer object
	//Doubles as Flight Event object
	public static class Person implements Comparable<Person>
	{
		public boolean isPay = false;
		public boolean drink = false;
		public State state;
		public int lastCash;
		public Type type;
		public Type originalType;
		public double time;//States the time this event pops
		public double arrival;//States when this customers originally arrived
		public int numVendorsVisited;
		public ArrayList<Type> prevVen = new ArrayList<Type>();
		public int numVendersDesired;
		//Customer data container/event object
		
		public Person(double a, Type b, boolean d, int e, int k)
		{
			drink = d;
			time = a;
			arrival = a;
			type = b;
			originalType = b;
			numVendersDesired =e;
			state = State.NOTARRIVED;
			numVendorsVisited=k;
		}
		
		@Override
		public int compareTo(Person c1) 
		{
			return (int)(this.time - c1.time);
        }
	}

	//Object that contains the check in with two lines
	//The data for Agent is stuck in here by itself, the object keeps track of how much time it was idle
	public static class Eatery
	{
		LinkedList<Person> line;
		int cashName; 
		Type type;
		boolean busy;
		double maxTime;
		
		public Eatery(Type t, double ti, int n)
		{
			busy = false;
			type = t;
			maxTime = ti;
			cashName = n;
			
			line = new LinkedList<Person>();
                        
		}
		 
		public void arrive(Person per)
		{
			boolean processed = false;
			boolean ate = false;
		
			if(!busy)
				processPerson(per);
			else
				line.add(per);			
		}

		public void popLine(Person per)
		{
			busy = false;
			if(!line.isEmpty())
				processPerson(line.pop());
		}
		
		public void processPerson(Person per)
		{		
			per.time = time;
			busy = true;
			switch(type)
			{
				case PIZZA:
					per.time += generateTime(.143);
					break;
				case TACO: 
					per.time += generateTime(2.894);
					break;
				case STRUTTERS:
					per.time += generateTime(1.662);
					break;
				case SPECIAL:
					per.time += generateTime(.894);
					break;
				case SALAD:
					per.time += generateTime(3.121);
					break;
			}
			//**********************************TAKYEE EDITED*********************************************
			per.numVendorsVisited++;
			//**********************************TAKYEE EDITED*********************************************
			
			//System.out.println("Changes the state to bought");
			per.state= State.BOUGHT;
			
			if(oldCheckout && type == Type.DRINK)
				per.state = State.DRINK;
								
			eventQueue.add(per);
		}
	}
	
	public static class Cashier
	{
		Person lastOccupant;
		LinkedList<Person> line = new LinkedList<Person>();
		int name;
		Type type;
		boolean busy;
		double lastTime;

		public Cashier(Type t, int n)
		{
			type = t;
			name = n;
			busy = false;
			lastTime = 0;
		}
		
		public Cashier(int n)
		{
			name = n;
			busy = false;
			lastTime = 0;
		}
		
		public void arrive(Person per)
		{
			
			boolean processed = false;
		
			if(!busy)
				processPerson(per);
				
			else
				line.add(per);
		}

		public void popLine(Person per)
		{
			busy = false;
			if(!line.isEmpty())
				processPerson(line.pop());
		}
		
		//Will take the person, generate how long it will take for him to go through, and make new event for his leaving.
		public void processPerson(Person per)
		{		
			per.time = time;
			per.lastCash = name;
			busy = true;
			per.time += generateTime(checkTime);
			
			if(!oldCheckout && per.drink)
				per.time += generateTime(drinkTime);
				
			per.state = State.CHECKOUT;
			eventQueue.add(per);
		}		
	}
	
	//*****************************************************************TAKYEE WROTE THIS METHOD************************************
	

		
	public static class OldCashier
	{
		Person lastOccupant;
		LinkedList<Person> line = new LinkedList<Person>();
		ArrayList<Cashier> cash = new ArrayList<Cashier>();
		Cashier cashOne;
		Cashier cashTwo;
		int name;
		Type type;
		boolean busy;
		double lastTime;

		public OldCashier()
		{
			cashOne = new Cashier(8);
			cashTwo = new Cashier(9);
			cash.add(cashOne);
			cash.add(cashTwo);
		}
		
		public void arrive(Person per)
		{			
			boolean processed = false;
			
			for(Cashier c : cash)
			{
				if(!c.busy && !processed)
				{
					processed = true;
					c.processPerson(per);
				}
			}
			
			if(!processed)
			{
				line.add(per);
			}
		}

		public void popLine(Person per)
		{
			for(Cashier c : cash)
			{
				if(per.lastCash == c.name)
				{
					c.busy = false;
				}
			}
	
			for(Cashier c : cash)
				if(!c.busy && !line.isEmpty())
				{
					c.processPerson(line.pop());
				}
		}
	}
	
	public static double generateTime(double min)
	{
		return (60 * (Math.log(1 - rand.nextDouble()) / -(1/min)));
	}
	
	public static double poisson(double mean)
	{
		Random random = new Random();
		double r = (-Math.log(1.0-random.nextDouble()/mean*60));
		return r;
	}
   
	public static boolean bernoulli(double p)
	{
		if(p < 0.0 || p > 1.0)
			 throw new IllegalArgumentException("Probablility must be between 0.0 and 0.1");
		
		return uniform() < p;
	}
	
	public static double uniform()
	{
		Random random = new Random();
		return random.nextDouble();
	}
	
	public static boolean bernoulli(double p, double q)
	{
		return bernoulli(0.5);
	}
	
	public static double getCusRate(int c)
	{
		if(c == 0)
			return .05;
		else if (c == 1)
			return .1;
		else if (c == 2)
			return .5;
		else if (c == 3)
			return 1;
		else if (c == 4)
			return 2.5;
		else if (c == 5)
			return 5;
		else if (c == 6)
			return 10;
		else if (c == 7)
			return 25;
			
		return 0;	
	}
	
	public static void resetCounts()
	{
		avgTaco = 0;
		numTaco = 0;
		avgPizza = 0;
		numPizza = 0;
		avgSalad = 0;
		numSalad = 0;
		avgSpecial = 0;
		numSpecial = 0;
		avgStrutters = 0;
		numStrutters = 0;
		numComposite = 0;
		avgComposite = 0;
		totalAvg = 0;
	}
	
	public static void hereBeJank(int two)
	{
		if(two == 0)
		{
			wr.print("\n" + "<div class=\"hero-unit\">" + "\n" + "<h1>Old Checkout</h1>");
			wr.print("\n" + "<table class=\"table\">" + "\n");
		}
		
		if(two == 1)
		{
			oldCheckout = false;
			wr.print("\n" + "<div class=\"hero-unit\">" + "\n" + "<h1>New Checkout</h1>");
			wr.print("\n" + "<table class=\"table\">" + "\n"); 
		}
		
		wr.println("<thead>");  
		wr.println("<tr>");  
		wr.println("<th>Arrival Rate</th>");  
		wr.println("<th>Avg Time TacoBell</th>");  
		wr.println("<th>Avg Time Pizza</th>");  
		wr.println("<th>Avg Time Nicola's</th>");  
		wr.println("<th>Avg Time Culinary Classic</th>");  
		wr.println("<th>Avg Time Strutters</th>");  
		if(multVenProb == 0)
			wr.println("<th>Avg Time Multiple Vendors</th>");  
		wr.println("<th>Avg Total Time</th>");  
		wr.println("</tr>");  
		wr.println("</thead>"); 
	}
	
	public static void getData()
	{
		System.out.println("data:");
		System.out.println("all: " + allPeople.size());
		
		for(Person p : allPeople)
		{
			
			totalAvg += (p.time - p.arrival);
				
			if(p.type == Type.TACO && p.numVendorsVisited == 1)
			{
				numTaco++;
				avgTaco += (p.time - p.arrival);
			}
			if(p.type == Type.PIZZA && p.numVendorsVisited == 1)
			{
				numPizza++;
				avgPizza += (p.time - p.arrival);
			}
			if(p.type == Type.SPECIAL && p.numVendorsVisited == 1)
			{
				numSpecial++;
				avgSpecial += (p.time - p.arrival);
			}
			if(p.type == Type.STRUTTERS && p.numVendorsVisited == 1)
			{
				numStrutters++;
				avgStrutters += (p.time - p.arrival);
			}
			if(p.type == Type.SALAD && p.numVendorsVisited == 1)
			{
				numSalad++;
				avgSalad += (p.time - p.arrival);
			}
			if(p.numVendorsVisited > 2)
			{
				numComposite++;
				avgComposite += (p.time - p.arrival);
			}
		}
	}
	
	public static void vendorChecker(Person per)
	{
		boolean foundNewType=false;
		while(foundNewType==false) //KEEP GOING UNTIL NEW TYPE DETERMINED
		{
		//System.out.println("IS THIS THE INFINITY THING");
			//5 different things
			//generate random number between 0 and 4
			//0 is pizza, 1 is taco, 2 is strutters, 3 is special, 4 is salad
			int newType = rand.nextInt(5); //5 is exclusive 
			switch(newType) //checking to see if this person's arraylist already has this type
			{
				//REMEMBER TO PUT NEW TYPE INTO ARRAYLIST******
				case 0: //pizza
					for(Type varName:per.prevVen)
					{
						if(varName.equals(Type.PIZZA))
						{
							foundNewType=false;//just for me to not get confused #redundant
							break;
						}
						else
						{
							foundNewType=true;
							per.type=Type.PIZZA;
							//SEND IT BACK INTO THE THING
							per.prevVen.add(per.type);
							per.numVendorsVisited++;
						   // processPerson(per);
							break;
						}
					}
					break;
					
				case 1: //taco
					for(Type varName:per.prevVen)
					{
						if(varName.equals(Type.TACO))
						{
							foundNewType=false;//just for me to not get confused #redundant
							break;
						}
						else
						{
							foundNewType=true;
							per.type=Type.TACO;
							per.prevVen.add(per.type);
							//processPerson(per);
							//SEND IT BACK INTO THE THING
							per.numVendorsVisited++;
							break;
						}
					}
					break;
				case 2: //strutters
					for(Type varName:per.prevVen)
					{
						if(varName.equals(Type.STRUTTERS))
						{
							foundNewType=false;//just for me to not get confused #redundant
							break;
						}
						else
						{
							foundNewType=true;
							per.type=Type.STRUTTERS;
							//SEND IT BACK INTO THE THING
							per.prevVen.add(per.type);
							per.numVendorsVisited++;
							//processPerson(per);
							break;
						}
					}
					break;
				case 3: // special
					for(Type varName:per.prevVen)
					{
						if(varName.equals(Type.SPECIAL))
						{
							foundNewType=false;//just for me to not get confused #redundant
							break;
						}
						else
						{
							foundNewType=true;
							per.type=Type.SPECIAL;
							//SEND IT BACK INTO THE THING
							per.prevVen.add(per.type);
							per.numVendorsVisited++;
							//processPerson(per);
							break;
						}
					}
					break;
				case 4://salad
					for(Type varName:per.prevVen)
					{
						if(varName.equals(Type.SALAD))
						{
							foundNewType=false;//just for me to not get confused #redundant
							break;
						}
						
						else
						{
							foundNewType=true;
							per.type=Type.SALAD;
							//SEND IT BACK INTO THE THING
							per.prevVen.add(per.type);
							per.numVendorsVisited++;
							//processPerson(per);
							break;
						}
					}
					break;
			}
		}
	   eventQueue.add(per);
	}


			
	public static PriorityQueue<Person> deepCopy(PriorityQueue<Person> q)
	{
		PriorityQueue<Person> copy = new PriorityQueue<Person>();
		for(Person p : q)
			copy.add(new Person(p.time, p.type, p.drink, p.numVendersDesired, p.numVendorsVisited));
		
		return copy;
	}
}