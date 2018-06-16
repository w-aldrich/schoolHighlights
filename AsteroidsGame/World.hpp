//
//  World.hpp
//  FinalProject
//
//  Created by William Aldrich & Ted Pochmara
//
//

#ifndef World_hpp
#define World_hpp

#include <vector>
#include "Player.hpp"
#include "Planet.hpp"
#include "Asteroids.hpp"
#include "Bullet.hpp"
#include <SFML/Graphics.hpp>


using namespace std;



///Allows creation of the World
class World
{    
 
public:
    ///world constructor
    World();

    ///vectors for all the bullets or asteroids in the world
    vector<Bullet> bulletsInWorld;
    vector<Asteroids> roidsInWorld;
    
    ///boolean to know if playing or not
    bool isPlaying;
    
    ///timers
    int bulletTimer;
    int roidTimer;
    int healthTimer;
    
    ///Allows the game to continue playing
    void isPlayingGame(Player& player, Planet& planet, sf::RenderWindow& newWorld, sf::Text& displayScore, sf::Text& planetHitpoints, sf::Text& playerHitpoints);
    
    void checkForHits(sf::RenderWindow& newWorld, Player& player, Planet& planet);
};

///This gets rid of any bullets outside the bounds of the window
///doesnt really need to be a template like we originally thought but dont really want to change it
template <typename T>
void remove(T& things, const sf::RenderWindow& newWorld){
    things.erase(remove_if(things.begin(), things.end(), [&newWorld](const auto& x){return (x.getPosX() > newWorld.getSize().x ) || (x.getPosX() < 0) || (x.getPosY() > newWorld.getSize().y) || (x.getPosY() < 0);}),things.end());
}

//draw the cosmos
void draw(sf::RenderWindow& newWorld, sf::Texture& cosmos);

///Game Over text screen
void gameOverScreen(sf::RenderWindow& newWorld, const Player& player);

///Allows Text to be set up in game
void setTextUp(sf::Text& text, const int& posX, const int& posY, const sf::RenderWindow& newWorld, const sf::Font& font);


#endif /* World_hpp */

