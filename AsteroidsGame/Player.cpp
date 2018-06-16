//
//  Player.cpp
//  FinalProject
//
//  Created by William Aldrich & Ted Pochmara
//
//

#include "Player.hpp"
#include "World.hpp"
#include "Bullet.hpp"
#include <SFML/Graphics.hpp>



using namespace std;


//create a new player in the middle of the screen and set the texture
Player::Player(sf::Texture& fighter, const sf::RenderWindow& newWorld)
{
    posX = newWorld.getSize().x/2 - 100;
    posY = newWorld.getSize().y/2;
    direction = 0;
    velocity = 0;
    
    ship = (sf::RectangleShape (sf::Vector2f(85, 140)));
    ship.setTexture(&fighter);
    
    //This makes the origin in the center of the ship instead of the top left corner (also effects bullets)
    ship.setOrigin(42.5, 70);
    
    score = 0;
    hitpoints = 3;
}

//get the x position
float Player::getPosX() const
{
    return posX;
}

//get the y position
float Player::getPosY() const
{
    return posY;
}

//get the angle the player is facing
float Player::getDirection() const
{
    return direction;
}

//get hitbox of the player
sf::FloatRect Player::getHitbox() const{
    return ship.getGlobalBounds();
}

//get the score
int Player::getScore() const
{
    return score;
}

//get how many hitpoints the player has left
int Player::getHitpoints() const
{
    return hitpoints;
}

//increase the score by one
void Player::increaseScore()
{
    score++;
}

//decrease the player hitpoints by one
void Player::decreaseHitpoints()
{
    hitpoints--;
}

//check if the player is moving, and move the player if they are by one frame
void Player::isMoving(const sf::RenderWindow& newWorld)
{
    //move forward
    if (sf::Keyboard::isKeyPressed(sf::Keyboard::W))
    {
        if (!(posY <=0))
            posY -= 10;
    }
    
    //move backward
    if (sf::Keyboard::isKeyPressed(sf::Keyboard::S))
    {
        if (!(posY >= newWorld.getSize().y-50))
            posY += 10;
    }
    
    //move left
    if (sf::Keyboard::isKeyPressed(sf::Keyboard::A))
    {
        if (!(posX <= 0))
            posX -= 10 ;
    }
    
    //move right
    if (sf::Keyboard::isKeyPressed(sf::Keyboard::D))
    {
        if (!(posX >= newWorld.getSize().x-50))
            posX += 10;
    }
    
    //rotate the ship left
    if (sf::Keyboard::isKeyPressed(sf::Keyboard::Q))
        ship.setRotation(direction += 5);
    
    //rotate the ship right
    if (sf::Keyboard::isKeyPressed(sf::Keyboard::E))
        ship.setRotation(direction -= 5);
}

//draw the player on the screen
void Player::draw(sf::RenderWindow& newWorld)
{
    ship.setPosition(posX, posY);
    newWorld.draw(ship);
}

//check if the player is shooting
bool Player::shoot()
{
    return (sf::Keyboard::isKeyPressed(sf::Keyboard::Space));
}






