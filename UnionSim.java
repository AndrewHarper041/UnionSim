//Andrew Harper adh41
import java.util.*;
import java.io.*;

//NEED TO ADJUST TIME TO UNION HOURS 
//NEED TO MAKE TIME DISTRIBUTION BASED ON TIME OF DAY
//NEED TO RANDOMIZE THE EATERY
//NEED TO HANDLE BOTH STYLE OF CHECKOUT

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
	static double checkTime = .2;
	static double drinkTime = .2;
	static ArrayList<Eatery> eaterys = new ArrayList<Eatery>();
	static ArrayList<Cashier> cashiers = new ArrayList<Cashier>();
	static OldCashier oldCashier = new OldCashier();
	static boolean oldCheckout = false;
	
	//Generate all objects needed for simulation

	/*static Eatery pizzaHut = new Eatery(Type.PIZZA, 37800); //0 - 37800 (10.5 hours)
	static Eatery special = new Eatery(Type.SPECIAL, 34200); //0 - 34200 (9.5 hours)
	static Eatery tacoBell = new Eatery(Type.TACO, 37800); //0 - 37800 (10.5 hours)
	static Eatery salad = new Eatery(Type.SALAD, 32400); //0 - 32400 (9 hours)
	static Eatery strutters = new Eatery(Type.STRUTTERS, 34200); //0 - 34200 (9.5 hours)
	static Eatery drink = new Eatery(Type.DRINK, 37800);*/
	
	
	
	public static void main(String args[])
	{
		eaterys.add(new Eatery(Type.PIZZA, 37800, 1));
		eaterys.add(new Eatery(Type.SPECIAL, 34200, 2));
		eaterys.add(new Eatery(Type.TACO, 37800, 3));
		eaterys.add(new Eatery(Type.SALAD, 32400, 4));
		eaterys.add(new Eatery(Type.STRUTTERS, 34200, 2));
		eaterys.add(new Eatery(Type.DRINK, 37800, 5));
		
		
		//Get user input on # of days
		String input = "";
		Scanner sc = new Scanner(System.in);
		System.out.print("Simulate how many days?");
		days = sc.nextInt();
		System.out.print("Old(0) or new(1) style?");
		int cC = sc.nextInt();
		System.out.print("Rate people come in?");
		int customerRate = sc.nextInt();
		
		if(cC == 0)
			oldCheckout = true;

		Person per;
		
		for(int i = 1; i <= days; i++)
		{
			dayCount++;
			double fTime = 0;
			
			//Generate this days worth of customers and place in queue
			generateCustomer(customerRate);
			
			System.out.println("Customer Num for this day: " + eventQueue.size());
			
			
			//System.out.println("event peek " + eventQueue.peek().time);
			int numInter;
			while(!eventQueue.isEmpty() && eventQueue.peek().time <= 37800 * dayCount)
			{
				per = eventQueue.poll();
					
				time = per.time;
				
				handleEvent(per);
			}
			System.out.println("time " + time);
			System.out.println("events " + eventQueue.size());
			System.out.println("people fed " + allPeople.size());
			System.out.println("checkout line " + oldCashier.line.size());
			for(Eatery e : eaterys)
				System.out.println(e.type + " line " + e.line.size());
		}
	}
	
	//-----------------------------------------------FUNCTIONS---------------------------------------------------------
	
	public static void handleEvent(Person per)
	{
		
		//Normal customer events
		if(oldCheckout)
		{
			switch(per.state)
			{
				case NOTARRIVED: //People arrive, and are processed or placed in line
					//System.out.println(per.type);
					for(Eatery e : eaterys)
					{
						//System.out.println(e.type);
						if(per.type == e.type)
						{
							//System.out.println("people arrived");
							e.arrive(per);
						}
					}
					break;
					
				case BOUGHT: //People leave food place, POPING the line, and sent to checkout
					for(Eatery e : eaterys)
						if(per.type == e.type)
							e.popLine(per);
						
					if(!per.drink)
					{
						oldCashier.arrive(per);
					}
					
					else if(per.drink)
					{
						for(Eatery e : eaterys)
							if(e.type == Type.DRINK)
								e.arrive(per);
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
					//System.out.println(per.type);
					for(Eatery e : eaterys)
					{
						//System.out.println(e.type);
						if(per.type == e.type)
						{
							//System.out.println("people arrived");
							e.arrive(per);
						}
					}
					break;
				case BOUGHT: //People leave food place, POPING the line, and sent to checkout
					for(Eatery e : eaterys)
						if(per.type == e.type)
						{
							//System.out.println("people bought food");
							e.popLine(per);
							e.cashier.arrive(per);
						}
					break;
					
				case CHECKOUT: //People leave food place, POPING the line, and being placed in Drink process if oldCheckout.
					for(Eatery e : eaterys)
						if(per.type == e.type)
							e.cashier.popLine(per);
					
					allPeople.add(per);
					break;
			}
		}
	}

	
	//Generates a days customers, with arrival time and where they want to eat
	//Change lambda based on time of day for accurate distribution
	public static void generateCustomer(double lambda)
	{
		//System.out.println("Commute generate Event que" + eventQueue.size());
		double aTime = 0;
		double bags;
		int s = eventQueue.size();
		while(aTime <= 37800)
		{
			aTime += (-Math.log(1.0 - rand.nextDouble()) / lambda) * 60;//Generate time for next arrival * 60 to convert to sec
			eventQueue.add(new Person(aTime + (37800 * (dayCount - 1)), getType(), getDrink())); 
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
		public double time;//States the time this event pops
		public double arrival;//States when this customers originally arrived
		
		//Customer data container/event object
		public Person(double a, Type b, boolean d)
		{
			drink = d;
			time = a;
			arrival = a;
			type = b;
			state = State.NOTARRIVED;
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
		Cashier cashier;
		LinkedList<Person> line;
		Type type;
		boolean busy;
		double maxTime;
		
		public Eatery(Type t, double ti, int n)
		{
			busy = false;
			type = t;
			maxTime = ti;
			
			int temp = 0;
			if(t == Type.PIZZA)
				temp = 1;
			if(t == Type.DRINK)
				temp = 2;
			if(t == Type.TACO)
				temp = 3;
			if(t == Type.SALAD)
				temp = 4;
			if(t == Type.STRUTTERS || t == Type.SPECIAL)
				temp = 5;
				
 			cashier = new Cashier(t, temp);
					
			
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
			busy = true;
			switch(type)
			{
				case PIZZA:
					per.time += generateTime(.1);
					break;
				case TACO: 
					per.time += generateTime(3);
					break;
				case STRUTTERS:
					per.time += generateTime(2);
					break;
				case SPECIAL:
					per.time += generateTime(1);
					break;
				case SALAD:
					per.time += generateTime(5);
					break;
			}
			
			per.state = State.BOUGHT;
			//If NOT old and person has drink then add drink time
			if(type == Type.DRINK)
				per.state = State.DRINK;
				
			if(!oldCheckout && per.drink)
				per.time += generateTime(.2);
				
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
			per.lastCash = name;
			busy = true;
			per.time += generateTime(checkTime);
			if(!oldCheckout && per.drink)
				per.time += generateTime(drinkTime);
			per.state = State.CHECKOUT;
			eventQueue.add(per);
		}		
	}
	
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
	
				
			
			for(Cashier c : cashiers)
				if(!c.busy)
				{
					c.processPerson(line.pop());

				}
		}
		

	}
	
	public static double generateTime(double min)
	{
		return (60 * (Math.log(1 - rand.nextDouble()) / -(1/min)));
	}
	
}