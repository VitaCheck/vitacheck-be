package com.vitacheck.domain.purposes;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPurposeCategory is a Querydsl query type for PurposeCategory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPurposeCategory extends EntityPathBase<PurposeCategory> {

    private static final long serialVersionUID = -1480748102L;

    public static final QPurposeCategory purposeCategory = new QPurposeCategory("purposeCategory");

    public final NumberPath<Integer> displayOrder = createNumber("displayOrder", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath imageUrl = createString("imageUrl");

    public final ListPath<com.vitacheck.domain.mapping.IngredientCategory, com.vitacheck.domain.mapping.QIngredientCategory> ingredientCategories = this.<com.vitacheck.domain.mapping.IngredientCategory, com.vitacheck.domain.mapping.QIngredientCategory>createList("ingredientCategories", com.vitacheck.domain.mapping.IngredientCategory.class, com.vitacheck.domain.mapping.QIngredientCategory.class, PathInits.DIRECT2);

    public final EnumPath<AllPurpose> name = createEnum("name", AllPurpose.class);

    public QPurposeCategory(String variable) {
        super(PurposeCategory.class, forVariable(variable));
    }

    public QPurposeCategory(Path<? extends PurposeCategory> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPurposeCategory(PathMetadata metadata) {
        super(PurposeCategory.class, metadata);
    }

}

