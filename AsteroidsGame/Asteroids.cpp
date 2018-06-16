//
//  Asteroids.cpp
//  FinalProject
//
//  Created by Ted Pochmara & William Aldrich
//
//

#include "Asteroids.hpp"
#include "World.hpp"
#include <SFML/Graphics.hpp>
#include <cmath>
using namespace std;

#define PI 3.14159265

//constructor
Asteroids::Asteroids(sf::Texture& asteroid3, sf::RenderWindow& newWorld){
    asteroid = (sf::CircleShape (85, 8));
    asteroid.setTexture(&asteroid3);
    //asteroid.setFillColor(sf::Color::White);
    direction = 0;
    canDie = false;

    //spawn at random loc around/outside edge of screen
    int randomLoc = 0;
    randomLoc = rand()%4;
    if (randomLoc == 0){
        posX = rand()%newWorld.getSize().x;  //TODO: add/substract to get appearing offscreen
        posY = 0;
    }
    else if (randomLoc == 1){
        posX = 0;
        posY = rand()%newWorld.getSize().y;
    }
    else if (randomLoc == 2){
        posX = newWorld.getSize().x;
        posY = rand()%newWorld.getSize().y;
    }
    else{
        posX = rand()%newWorld.getSize().x;
        posY = newWorld.getSize().y;
    }
}

//takes window as ref param and draws ship to it
void Asteroids::draw(sf::RenderWindow& newWorld){
    posX += ((newWorld.getSize().x/2) - getPosX())/200;
    posY += ((newWorld.getSize().y/2) - getPosY())/200;
    
    asteroid.setPosition(posX, posY);
    newWorld.draw(asteroid);
    
}

//allows access to x position
float Asteroids::getPosX() const{
    return posX;
}
//allows access to y position
float Asteroids::getPosY() const{
    return posY;
}

//get hitbox roids
sf::FloatRect Asteroids::getHitboxR() const{
    return asteroid.getGlobalBounds();
}


void Asteroids::resize()
{
    asteroid.setScale(.5f, .5f);
    canDie = true;
}
