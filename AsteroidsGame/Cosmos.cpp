//
//  Cosmos.cpp
//  final
//
//  Created by Ted Pochmara on 9/21/17.
//

#include "Cosmos.hpp"
#include "Planet.hpp"
#include "World.hpp"
#include "Player.hpp"
#include "Asteroids.hpp"
#include "Bullet.hpp"
#include <iostream>
#include <SFML/Graphics.hpp>


Cosmos::Cosmos(sf::RenderWindow& newWorld, sf::Texture& stars){
    cosmos.setTexture(stars);
}


void Cosmos::drawCosmos(sf::RenderWindow& newWorld)
{
    newWorld.draw(cosmos);
}
