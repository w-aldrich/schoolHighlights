//
//  Asteroids.hpp
//  FinalProject
//
//  Created by Ted Pochmara & William Aldrich
//
//

#ifndef Asteroids_hpp
#define Asteroids_hpp
#include <SFML/Graphics.hpp>

///Creates and maintains asteroids on screen
class Asteroids{
public:
    
    ///constructor basic
    Asteroids(sf::Texture& asteroid3, sf::RenderWindow& newWorld);
    ///takes window as ref param and draws ship to it
    void draw(sf::RenderWindow& newWorld);
    ///rotation of the asteroid
    float rotation;
    ///moves the roid
    void move(sf::RenderWindow& newWorld);
    
    //allows access to x position
    float getPosX() const;
    //allows access to y position
    float getPosY() const;
    ///The hitbox of the Asteroid
    sf::FloatRect getHitboxR() const;
    
    ///resize the asteroid
    void resize();
    ///says if the asteroid can die
    bool canDie;

private:
    
    float posX;
    float posY;
    float direction;
    float velocity;
    sf::CircleShape asteroid;
};



#endif /* Asteroids_hpp */
