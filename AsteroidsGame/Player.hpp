//
//  Player.hpp
//  FinalProject
//
//  Created by William Aldrich & Ted Pochmara
//
//

#ifndef Player_hpp
#define Player_hpp
#include <SFML/Graphics.hpp>


///information for the player
class Player
{
    //x position
    float posX;
    //y position
    float posY;
    
    //how fast the player is moving (will implement later)
    float velocity;
    
    //the angle that the ship is facing
    float direction;
    
    //the textured ship
    sf::RectangleShape ship;
    
    //the player score
    int score;
    
    //how many hitpoints the player has left
    int hitpoints;
    
public:
    ///allows access to x position
    float getPosX() const;
    
    ///allows access to y position
    float getPosY() const;
    
    ///need to get globalbounds of private sf::RectangleShape ship
    sf::FloatRect getHitbox() const;
    
    ///return the angle the player is facing
    float getDirection() const;
    
    ///return the score of the player
    int getScore() const;
    
    ///return the hitpoints of the player
    int getHitpoints() const;
    
    ///decrease the hitpoints by one
    void decreaseHitpoints();
    
    ///increases the player score by one
    void increaseScore();
    
    ///check if the player is moving or not and move the player
    void isMoving(const sf::RenderWindow& newWorld);
    
    ///constructor
    Player(sf::Texture& fighter, const sf::RenderWindow& newWorld);
    
    ///draw the player
    void draw(sf::RenderWindow& newWorld);
    
    ///checks to see if the player is pressing the space bar
    bool shoot();
};

#endif /* Player_hpp */

