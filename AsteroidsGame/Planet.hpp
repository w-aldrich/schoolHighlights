//
//  Planet.hpp
//  final
//
//  Created by Ted Pochmara & William Aldrich
//

#ifndef Planet_hpp
#define Planet_hpp
#include <SFML/Graphics.hpp>

///creates and maintains the planet in the middle of the screen
class Planet{
public:
    ///constructor
    Planet(sf::Texture& globe, const sf::RenderWindow& newWorld);
    
    ///draw the planet to the screen
    void draw(sf::RenderWindow& newWorld);
    
    ///the hitbox of the planet
    sf::FloatRect getHitboxP() const;
    
    ///how many times the planet can be hit
    int planetLife;

private:
    
    float posX;
    float posY;
    
    sf::RectangleShape planet;
    
};

#endif /* Planet_hpp */
