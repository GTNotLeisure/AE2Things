package com.asdflj.ae2thing.api.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * minimal copy of {@link com.gtnewhorizon.gtnhlib.config.Config}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Config {

    /**
     * The mod id that this configuration is associated with.
     */
    String modid();

    /**
     * Root element category, defaults to "general".
     */
    String category() default "general";

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.FIELD, ElementType.TYPE })
    @interface LangKey {

        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.FIELD, ElementType.TYPE })
    @interface Comment {

        String[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface DefaultBoolean {

        boolean value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface DefaultInt {

        int value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface RangeInt {

        int min() default Integer.MIN_VALUE;

        int max() default Integer.MAX_VALUE;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Name {

        String value();
    }

    /**
     * Fields or classes annotated with this will automatically be synced from server -> client. If applied to a class,
     * all fields (including subcategories) in the class will be synced. All fields are restored to their original value
     * when the player disconnects.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.FIELD, ElementType.TYPE })
    @interface Sync {

        boolean value() default true;
    }
}
