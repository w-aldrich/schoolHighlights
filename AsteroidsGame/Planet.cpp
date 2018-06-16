//
//  Planet.cpp
//  final
//
//  Created by Ted Pochmara & William Aldrich
//

#include "Planet.hpp"
#include "Player.hpp"
#include "World.hpp"
#include "Bullet.hpp"
#include <SFML/Graphics.hpp>

Planet::Planet(sf::Texture& globe, const sf::RenderWindow& newWorld){
    posX = 900;
    posY = 700;
    planetLife = 5;
    planet = (sf::RectangleShape (sf::Vector2f(100, 100)));
    planet.setTexture(&globe);
    planet.scale(sf::Vector2f(2.f, 2.f));
    
}

//draw the planet to the screen
void Planet::draw(sf::RenderWindow& newWorld)
{
    planet.setPosition(posX, posY);
    newWorld.draw(planet);
}

//return the hitbox of the planet
sf::FloatRect Planet::getHitboxP() const{
    return planet.getGlobalBounds();
}
