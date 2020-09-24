package com.lambdaschool.secretfamilyrecipe.services;

import com.lambdaschool.secretfamilyrecipe.exceptions.ResourceNotFoundException;
import com.lambdaschool.secretfamilyrecipe.models.*;
import com.lambdaschool.secretfamilyrecipe.repository.CategoryRepository;
import com.lambdaschool.secretfamilyrecipe.repository.IngredientRepository;
import com.lambdaschool.secretfamilyrecipe.repository.RecipeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Transactional
@Service("recipeService")
public class RecipeServiceImpl implements RecipeService {

    @Autowired
    UserAuditing userAuditing;

    @Autowired
    UserService userService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    RecipeRepository recipeRepository;

    @Autowired
    HelperFunctions helperFunctions;

    @Autowired
    IngredientService ingredientService;

    @Autowired
    IngredientRepository ingredientRepository;

    @Autowired
    CategoryRepository catrepos;

    @Override
    public List<Recipe> findAll() {
        List<Recipe> list = new ArrayList<>();
        recipeRepository.findAll()
                .iterator()
                .forEachRemaining(list::add);
        return list;
    }

    @Override
    public Recipe findRecipeById(long recipeid) {
        return recipeRepository.findById(recipeid)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe with id " + " Not Found!"));
    }

    @Transactional
    @Override
    public void delete(long id) {
        if (recipeRepository.findById(id).isPresent()) {
            recipeRepository.deleteById(id);
        } else {
            throw new ResourceNotFoundException("Recipe with id " + " Not Found");
        }

    }

    @Transactional
    @Override
    public Recipe update(Recipe updateRecipe, long recipeid) {


        Recipe currentRecipe = findRecipeById(recipeid);
            if (updateRecipe.getTitle() != null) {
                currentRecipe.setTitle((updateRecipe.getTitle()));
            }
            if (updateRecipe.getSource() != null) {
                currentRecipe.setSource(updateRecipe.getSource());
            }
            if (updateRecipe.getPreptime() != null){
                currentRecipe.setPreptime(updateRecipe.getPreptime());
            }
            if (updateRecipe.getIngredients().size() > 0) {
                currentRecipe.getIngredients().clear();
                for (RecipeIngredients ri : updateRecipe.getIngredients()) {
                    Ingredient addIngredient = ingredientRepository.findById(ri.getIngredient().getIngredientid())
                            .orElseThrow(() -> new ResourceNotFoundException("Ingredient id " + ri.getIngredient().getIngredientid() + " not found!"));

                    currentRecipe.getIngredients()
                            .add(new RecipeIngredients(currentRecipe, addIngredient));
                }
            }
            if (updateRecipe.getCategories().size() > 0) {
                currentRecipe.getCategories().clear();
                for (RecipeCategory rcat : updateRecipe.getCategories()) {
                    Category addCat = catrepos.findById(rcat.getCategory().getCategoryid())
                            .orElseThrow(() -> new ResourceNotFoundException("Category id " + rcat.getCategory().getCategoryid() + " Not Found"));

                    currentRecipe.getCategories().add(new RecipeCategory(currentRecipe, addCat));
                }
            }
            if (updateRecipe.getInstruction() != null) {
                currentRecipe.setInstruction(updateRecipe.getInstruction());
            }
            if (updateRecipe.getUser() != null) {
                currentRecipe.setUser(updateRecipe.getUser());
            }
            return recipeRepository.save(currentRecipe);
    }

    @Transactional
    @Override
    public Recipe save(Recipe recipe) {
        Recipe newRecipe = new Recipe();

//        User currentUser = userService.findUserById(userid);
        if (recipe.getRecipeid() != 0) {
            recipeRepository.findById(recipe.getRecipeid())
                    .orElseThrow(() -> new ResourceNotFoundException("Recipe id " + recipe.getRecipeid() + " not found!"));
        }
        newRecipe.setTitle(recipe.getTitle());
        newRecipe.setSource(recipe.getSource());
        newRecipe.setPreptime(recipe.getPreptime());
        newRecipe.setInstruction(recipe.getInstruction());

        newRecipe.setIngredients(new HashSet<>());
        for (RecipeIngredients ri : recipe.getIngredients()) {
            Ingredient addIng;
            if (ri.getIngredient().getIngredientid() > 0) {
                addIng = ingredientRepository.findById(ri.getIngredient().getIngredientid())
                        .orElseThrow(() -> new ResourceNotFoundException("Ingredient id " + ri.getIngredient().getIngredientid()));
            } else {
                Ingredient newIng = new Ingredient();
                newIng.setName(ri.getIngredient().getName());
                newIng.setAmount(ri.getIngredient().getAmount());
                addIng = ingredientService.save(newIng);
            }

            newRecipe.getIngredients().add(new RecipeIngredients(newRecipe, addIng));
        }

        newRecipe.setCategories(new HashSet<>());
        for (RecipeCategory rcat : recipe.getCategories()) {
            Category addCat;
            if (rcat.getCategory().getCategoryid() > 0) {
                addCat = catrepos.findById(rcat.getCategory().getCategoryid())
                        .orElseThrow(() -> new ResourceNotFoundException("Category id " + rcat.getCategory().getCategoryid() + " Not Found"));
            } else {
                Category newCat = new Category();
                newCat.setCategoryname(rcat.getCategory().getCategoryname());
                addCat = categoryService.save(newCat);
            }
            newRecipe.getCategories().add(new RecipeCategory(newRecipe, addCat));
        }
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            newRecipe.setUser(recipe.getUser());
        } else {
            newRecipe.setUser(userService.findByName(auth.getName()));
        }

        return recipeRepository.save(newRecipe);
    }

}