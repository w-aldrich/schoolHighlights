//
//  Bullet.cpp
//  FinalProject
//
//  Created by William Aldrich & Ted Pochmara
//
//

#include "Bullet.hpp"
#include <SFML/Graphics.hpp>
#include <cmath>

#define PI 3.14159265

//set the starting point of the bullet to whatever direction and x,y coordinate the player is at
Bullet::Bullet(Player player)
{
    posY = player.getPosY();
    posX = player.getPosX();
    
    direction = player.getDirection() -90;
    
    bullet = (sf::CircleShape (10));
    bullet.setFillColor(sf::Color::Green);
}

//draw the bullet and travel along the screen
void Bullet::draw(sf::RenderWindow& newWorld)
{
    posX+= (15 * cosf(direction * PI / 180));
    posY += (15 * sinf(direction * PI / 180));
    bullet.setPosition(posX, posY);

    
    newWorld.draw(bullet);
}

//allows access to x position
float Bullet::getPosX()const{
    return posX;
}
//allows access to y position
float Bullet::getPosY()const{
    return posY;
}

//allows acces to the hitbox of the bullet
sf::FloatRect Bullet::getHitboxB()
{
    return bullet.getGlobalBounds();
}
















