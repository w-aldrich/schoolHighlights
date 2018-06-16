//
//  Cosmos.hpp
//  final
//
//  Created by Ted Pochmara on 9/21/17.
//

#ifndef Cosmos_hpp
#define Cosmos_hpp
#include <SFML/Graphics.hpp>

class Cosmos{
    sf::Sprite cosmos;
public:
    Cosmos(sf::RenderWindow& newWorld, sf::Texture& stars);
    void drawCosmos(sf::RenderWindow& newWorld);

};

#endif /* Cosmos_hpp */
