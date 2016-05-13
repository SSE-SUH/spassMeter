package de.uni_hildesheim.sse.monitoring.runtime.configuration;

import java.util.Iterator;

import de.uni_hildesheim.sse.monitoring.runtime.utils.HashMap;

/**
 * Defines a static profile for an execution environment, i.e. a simple and 
 * convenient way to initialize configuration settings.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public abstract class Profile {

    /**
     * Stores the name-profile mappings.
     */
    private static final HashMap<String, Profile> PROFILES 
        = new HashMap<String, Profile>();
    
    /**
     * Registers a profile.
     * 
     * @param profile the profile to be registered
     * 
     * @throws IllegalArgumentException if <code>profile</code> is <b>null</b>
     *   or empty
     * 
     * @since 1.00
     */
    public static final void registerProfile(Profile profile) {
        if (null == profile) {
            throw new IllegalArgumentException("'profile' must not be null");
        }
        String name = profile.getName();
        if (null == name || 0 == name.length()) {
            throw new IllegalArgumentException("the name of the profile must " 
                + "neither be null or empty");
        }
        PROFILES.put(profile.getName(), profile);
    }
    
    /**
     * Returns the profile matching the given <code>name</code>.
     * 
     * @param name the name of the profile to be returned
     * @return the profile or <b>null</b> if none was found
     * 
     * @throws IllegalArgumentException if <code>name</code> is <b>null</b>
     *   
     * @since 1.00
     */
    public static final Profile getProfile(String name) {
        if (null == name) {
            throw new IllegalArgumentException("'name' must not be null");
        }
        return PROFILES.get(name);
    }
    
    /**
     * Returns all registered profiles.
     * 
     * @return all registered profiles
     * 
     * @since 1.00
     */
    public static final Iterator<Profile> getProfiles() {
        return PROFILES.values().iterator();
    }
    
    /**
     * Returns the name of the profile as it should be specified in 
     * configuration options.
     * 
     * @return the name of the profile.
     * 
     * @since 1.00
     */
    public abstract String getName();

    /**
     * Applies the default settings of this profile to the configuration.
     * 
     * @since 1.00
     */
    public abstract void applyConfiguration();
    
}