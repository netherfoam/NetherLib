NetherLib
=========

A collection of my personal Java libraries I've written, as convenience for other projects I write. They include event handling, some stream IO wrappers, a reflection class loader, a TrieSet for string completion, SQL data model and YML Configuration API for the SnakeYML.jar lib included in lib/

== Configs ==
The configs in this repository make use of the SnakeYML configuration library, allowing a nicer API to be used for setting and retrieving primitive values from a configuration file. Values include all primitive types (boolean, int, long, float, double, byte, short), Strings, maps, lists and ConfigSections, which may represent more advanced objects which have a unique key (Such as a player's items, having a username as a key, and an item slot as a subkey)

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

