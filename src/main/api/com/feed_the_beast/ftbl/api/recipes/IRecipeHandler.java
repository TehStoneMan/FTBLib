package com.feed_the_beast.ftbl.api.recipes;

/**
 * Created by LatvianModder on 23.08.2016.
 */
public interface IRecipeHandler
{
    boolean isActive();

    void loadRecipes(IRecipes recipes);
}
