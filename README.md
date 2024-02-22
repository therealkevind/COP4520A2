# Compiling and running
To compile this project, first ensure JDK 8 or later is installed. Then, open a terminal in this directory, and execute:
```sh
javac -d ./bin/ ./src/*.java
```

To then run the program, execute:
```sh
java -cp ./bin App N [guestCount [seed]]
```
where:
- `N` is the problem to simulate, either `1` or `2`.
- `guestCount` is the number of guests to simulate - default `8`.
- `seed` is a random seed to use, if you want reproducible results.

# Problem 1
## The problem
An implicit assumption is that the guests don't notice others being asked to visit the labyrinth (or being missing for suspiciously long times); otherwise this would be trivial.

I'm also going to assume that guests will be asked to visit until someone announces everyone's visited. More specifically, I'll assume each guest visits arbitrarily often. Otherwise, no strategy is guaranteed to work (for any strategy, there would be some orderings of guest visits for which it either has guests announce despite not everyone having visited or never has guests announce despite everyone having visited at least once).

In terms of memory, the guests have a single bit of shared information (whether there's a cupcake), along with an internal state based on their behavior on their previous visits to the labyrinth, if any. On each visit the guests may optionally toggle the shared bit (note that requesting a new cupcake and immediately eating it is a no-op) and update their state, and may announce if they've all visited (one of them *must* do this at some point once they all have).

## The strategy
Before the game begins, the guests elect a leader. Then, after the game begins:
- Everyone who *isn't* the leader eats a cupcake the first time they see one, and does nothing thereafter.
- The leader requests a cupcake every time they see an empty plate, and keeps track of how many times they do so, while ignoring any cupcakes they find.  
  After they've requested a cupcake *N*-1 times, they announce that everyone's visited (and optionally eat the cupcake that's brought, as no one else will).  
  (In other words: If *N* is 1, they announce on their first visit because they've already requested cupcakes 0 times; otherwise, they announce immediately after requesting the (*N*-1)th cupcake.)

There's a similar strategy possible where the leader instead gorges themself on cupcakes the other guests provide, but it's less efficient: it requires at least as many visits, and slightly more on average (except in the trivial *N*=1 case) because their initial visit can reveal nothing about other guests. (It also has the side effect of the leader being the only one to eat cupcakes, and them having to eat *N* cupcakes.)

## Example
In my implementation, guest 0 is always the leader.
```
> java -cp ./bin App 1 3 31
Guest 0 leaves the cupcake.
Guest 2 eats the cupcake.
Guest 0 requests a cupcake.
Guest 2 leaves the cupcake.
Guest 0 leaves the cupcake.
Guest 1 eats the cupcake.
Guest 1 leaves the plate empty.
Guest 0 announces that all guests have visited!
  All 3 guests have visited!
```
Notice that the leader never eats the cupcake (or at least not until after the game and simulation end), and that the other guests both encounter a cupcake multiple times but only eat it the first time.

# Problem 2
An implicit assumption is that the guests will prioritize allowing everyone who wants to see the vase to do so at some point.

## The strategies
1. As the problem suggests, this is prone to starvation.
2. This is also prone to starvation; if a guest wants to see the vase but is slow to check the door, they may never reach it before someone else goes inside.
3. This strategy solves the starvation issue. But there are two issues:
   - The first guest in line, if they enter the queue while there's no one inside, will never get notified that the showroom is available. So they'll have to check for that themself.  
     (Guests need only check if they enter an empty queue; otherwise, the person ahead of them in line will be responsible for notifying them on their turn.)
   - Guests in the queue cannot also enjoy the rest of the party; if there are a lot of guests interested in the vase they'll be spending a lot of time standing in line.
     This issue is shared with strategy 1, but strategy 2 lacks this issue.  
     However, this isn't as important as fairness, so this strategy is better.

So the guests should choose strategy 3.