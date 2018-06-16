//
//  Bullet.hpp
//  FinalProject
//
//  Created by William Aldrich & Ted Pochmara
//
//

#ifndef Bullet_hpp
#define Bullet_hpp

#include <stdio.h>
#include "Player.hpp"
#include <SFML/Graphics.hpp>

///creates and maintains bullets shot from the player ship
class Bullet
{
    float posX;
    
    float posY;
    
    float direction;
    
    sf::CircleShape bullet;
    
public:
    
    ///constructor
    Bullet(Player player);
    
    ///draw the bullet to the screen
    void draw(sf::RenderWindow& newWorld);
    
    ///allows access to x position
    float getPosX()const;
    ///allows access to y position
    float getPosY()const;
    
    ///get the hitbox for the bullet
    sf::FloatRect getHitboxB();

};

#endif /* Bullet_hpp */
