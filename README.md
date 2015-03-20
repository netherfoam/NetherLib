NetherLib
=========

A collection of my personal Java libraries I've written, as convenience for other projects I write. They include event handling, some stream IO wrappers, a reflection class loader, a TrieSet for string completion, SQL data model and YML Configuration API for the SnakeYML.jar lib included in lib/

== Configs ==

The configs in this repository make use of the SnakeYML configuration library, allowing a nicer API to be used for setting and retrieving primitive values from a configuration file. Values include all primitive types (boolean, int, long, float, double, byte, short), Strings, maps, lists and ConfigSections, which may represent more advanced objects which have a unique key (Such as a player's items, having a username as a key, and an item slot as a subkey). Example usage is somewhat like this:<pre>
//Constructs a new config file
FileConfig config = new FileConfig(new File("config.yml"));
//Loads data from disk for the first time (Else it will be a blank file)
config.reload();
//Get the old value, default to 6
int old = config.getInt("value", 6); 
//Updates the config
config.set("value", old + 10);

ConfigSection section = new ConfigSection();
section.set("home", "Brisbane");
section.set("age", 21);

//Put the section into the main file under "subsection"
config.set("subsection", section);

//Writes config to disk again
config.save();

The result will be something like:
value: 16
subsection:
   home: Brisbane
   age: 21
</pre>

== AreaGrid ==

A set of classes used for handling Spatial Indexing for a 2D area. A class has to implement MBR, and then it can be added to an AreaGrid. MBR stands for Minimum Bounds Rectangle, the smallest rectangle that covers all of a the given object.  A large number of these MBR objects can be added to the AreaGrid. Later, a query can be performed on the MBR, by creating another MBR with dimensions and positions that represent the area you wish to search. AreaGrid.get(MBR query) returns a HashSet of all MBR's which overlap with the supplied query, much faster than a linear search of all objects from (say) a list.

A key limitation in this AreaGrid class is that MBR's may not have negative positions, and the MBR is not resizable after construction.

== Events ==

A set of classes for handling a publish-subscribe system. A class that implements EventListener may be defined, and then methods which take a single Event argument of any type. The method may have any name, but must have an @EventHandler annotation above it. A new instance of the class is instantiated and registered with the EventMananger.

An event is then triggered at some point, of any class that extends Event. The code generating the event then calls EventManager.call(event), notifying all EventListeners which have @EventHandler methods that accept the given type of event.

The class is a standalone implementation I've written, inspired by Bukkit's event handling. I wrote this because it is a neat way of handling events for systems that require plugins or modules.

This supports unregistering listeners, cancelling events through the Cancellable interface and priority (Part of the @EventHandler annotation). An example:
<pre>
class MyListener implements EventListener{
   @EventHandler(priority = EventPriority.NORMAL)
   public void my_method_name(PlayerDeathEvent e){
       if(e.getPlayer().getName().equalsIgnoreCase("netherfoam")){
          e.getPlayer().setHealth(999);
       }
   }
}

class Main{
  public static void main(String[] args){
     EventManager m = new EventManager();
     MyListener listener = new MyListener();
     m.register(listener);
     
     PlayerDeathEvent event = new PlayerDeathEvent(..., ...);
     m.call(event); //Will call my_method_name()
  }
}
</pre>

== TrieSet ==

A useful utility for autocompletion. This class allows a developer to add a number of Strings to the set, then at a later point enter a prefix that the String contains which can be used to retrieve all Strings which have been added to the set that start with the given prefix. It does not use a linear search algorithm. Example,
<pre>
TrieSet set = new TrieSet();
s.add("alphabet");
s.add("alpaca");
s.add("beta");

s.matches("alp"); //Returns a HashSet<String> of ["alphabet", "alpaca"]
s.matches("beta"); //Returns a HashSet<String> of ["beta"]
</pre>

