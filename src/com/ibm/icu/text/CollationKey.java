/**
*******************************************************************************
* Copyright (C) 1996-2002, International Business Machines Corporation and    *
* others. All Rights Reserved.                                                *
*******************************************************************************
*
* $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/icu/text/CollationKey.java,v $ 
* $Date: 2002/06/21 23:56:44 $ 
* $Revision: 1.6 $
*
*******************************************************************************
*/
package com.ibm.icu.text;

import java.util.Arrays;

/**
 * <p>
 * A <code>CollationKey</code> represents a <code>String</code> under the
 * rules of a specific <code>Collator</code> object. Comparing two
 * <code>CollationKey</code>s returns the relative order of the
 * <code>String</code>s they represent.
 * </p>
 * <p>
 * <code>CollationKey</code> instances can not be create directly. Rather, 
 * they are generated by calling <code>Collator.getCollationKey(String)</code>. 
 * Since the rule set of each <code>Collator differs</code>, the sort orders of 
 * the same string under two unique <code>Collator</code> may not be the same. 
 * Hence comparing <code>CollationKey</code>s generated from different 
 * <code>Collator</code> objects may not give the right results.
 * </p>
 * <p>
 * Similar to <code>CollationKey.compareTo(CollationKey)</code>, 
 * the method <code>RuleBasedCollator.compare(String, String)</code> compares
 * two strings and returns the relative order. During the construction
 * of a <code>CollationKey</code> object, the entire source string is examined
 * and processed into a series of bits that are stored in the 
 * <code>CollationKey</code> object. Bitwise comparison on the bit sequences 
 * are then performed during <code>CollationKey.compareTo(CollationKey)</code>. 
 * This comparison could incurr expensive startup costs while creating 
 * the <code>CollationKey</code> object, but once the objects are created, 
 * binary comparisons are fast, and is recommended when the same strings are
 * to be compared over and over again. 
 * On the other hand <code>Collator.compare(String, String)</code> examines 
 * and processes the string only until the first characters differing in order,
 * and is recommend for use if the <code>String</code>s are to be compared only
 * once.
 * </p>
 * <p>
 * Details of the composition of the bit sequence is located at
 * <a href=http://oss.software.ibm.com/icu/userguide/Collate_ServiceArchitecture.html>
 * user guide</a>.
 * </p>
 * <p>The following example shows how <code>CollationKey</code>s might be used
 * to sort a list of <code>String</code>s.</p>
 * <blockquote>
 * <pre>
 * // Create an array of CollationKeys for the Strings to be sorted.
 * Collator myCollator = Collator.getInstance();
 * CollationKey[] keys = new CollationKey[3];
 * keys[0] = myCollator.getCollationKey("Tom");
 * keys[1] = myCollator.getCollationKey("Dick");
 * keys[2] = myCollator.getCollationKey("Harry");
 * sort( keys );
 * <br>
 * //...
 * <br>
 * // Inside body of sort routine, compare keys this way
 * if( keys[i].compareTo( keys[j] ) > 0 )
 *    // swap keys[i] and keys[j]
 * <br>
 * //...
 * <br>
 * // Finally, when we've returned from sort.
 * System.out.println( keys[0].getSourceString() );
 * System.out.println( keys[1].getSourceString() );
 * System.out.println( keys[2].getSourceString() );
 * </pre>
 * </blockquote>
 * </p>
 * @see Collator
 * @see RuleBasedCollator
 * @author Syn Wee Quek
 * @since release 2.2, April 18 2002
 * @draft 2.2
 */
public final class CollationKey implements Comparable 
{
	// public methods -------------------------------------------------------

	// public getters -------------------------------------------------------
	
    /**
     * Returns the source string that this CollationKey represents.
     * @return source string that this CollationKey represents
     * @draft 2.2
     */
    public String getSourceString() 
    {
        return m_source_;
    }

    /**
     * <p>
     * Duplicates and returns the value of this CollationKey as a sequence 
     * of big-endian bytes terminated by a null.
     * </p> 
     * <p>
     * If two CollationKeys could be legitimately compared, then one could 
     * compare the byte arrays of each to obtain the same result.
     * <pre>
     * byte key1[] = collationkey1.toByteArray();
     * byte key2[] = collationkey2.toByteArray();
     * int i = 0;
     * while (key1[i] != 0 && key2[i] != 0) {
     *	   int key = key1[i] & 0xFF;
     *     int targetkey = key2[i] & 0xFF;
     *     if (key &lt; targetkey) {
     *         System.out.println("String 1 is less than string 2");
     *         return;
     *     }
     *     if (targetkey &lt; key) {
     *         System.out.println("String 1 is more than string 2");
     *     }
     *     i ++;
     * }
     * int key = key1[i] & 0xFF;
     * int targetkey = key2[i] & 0xFF;
     * if (key &lt; targetkey) {
     *     System.out.println("String 1 is less than string 2");
     *     return;
     * }
     * if (targetkey &lt; key) {
     *     System.out.println("String 1 is more than string 2");
     *     return;
     * }
     * System.out.println("String 1 is equals to string 2");;
     * </pre>
     * </p>  
     * @return CollationKey value in a sequence of big-endian byte bytes 
     *         terminated by a null.
     * @draft 2.2
     */
    public byte[] toByteArray() 
    {
    	int length = 0;
    	while (true) {
    		if (m_key_[length] == 0) {
    			break;
    		}
    		length ++;
    	}
    	length ++;
    	byte result[] = new byte[length];
    	System.arraycopy(m_key_, 0, result, 0, length);
        return result;
    }

 	// public other methods -------------------------------------------------	
 	
    /**
     * <p>
     * Compare this CollationKey to the argument target CollationKey. 
     * The collation 
     * rules of the Collator object which created these keys are applied.
     * </p>
     * <p>
     * <strong>Note:</strong> Comparison between CollationKeys created by 
     * different Collators may not return the correct result. See class 
     * documentation.
     * </p>
     * @param target target CollationKey
     * @return an integer value, if value is less than zero this CollationKey
     *         is less than than target, if value is zero if they are equal 
     *         and value is greater than zero if this CollationKey is greater 
     *         than target.
     * @exception NullPointerException thrown when argument is null.
     * @see Collator#compare(String, String)
     * @draft 2.2
     */
    public int compareTo(CollationKey target)
    {
    	int i = 0;
    	while (m_key_[i] != 0 && target.m_key_[i] != 0) {
    		int key = m_key_[i] & 0xFF;
    		int targetkey = target.m_key_[i] & 0xFF;
    		if (key < targetkey) {
    			return -1;
    		}
    		if (targetkey < key) {
    			return 1;
    		}
    		i ++;
    	}
    	// last comparison if we encounter a 0
    	int key = m_key_[i] & 0xFF;
    	int targetkey = target.m_key_[i] & 0xFF;
        if (key < targetkey) {
    		return -1;
    	}
    	if (targetkey < key) {
    		return 1;
    	}
        return 0;
    }

    /**
     * <p>
     * Compares this CollationKey with the specified Object.
     * The collation 
     * rules of the Collator object which created these objects are applied.
     * </p>
     * <p>
     * See note in compareTo(CollationKey) for warnings of incorrect results
     * </p>
     * @param obj the Object to be compared.
     * @return Returns a negative integer, zero, or a positive integer 
     *         respectively if this CollationKey is less than, equal to, or 
     *         greater than the given Object.
     * @exception ClassCastException thrown when the specified argument is not 
     *            a CollationKey. NullPointerException thrown when argument 
     *            is null.
     * @see #compareTo(CollationKey)
     * @draft 2.2
     */
    public int compareTo(Object obj) 
    {
 		return compareTo((CollationKey)obj);
    }

    /**
     * <p>
     * Compare this CollationKey and the argument target object for equality.
     * The collation 
     * rules of the Collator object which created these objects are applied.
     * </p>
     * <p>
     * See note in compareTo(CollationKey) for warnings of incorrect results
     * </p>
     * @param target the object to compare to.
     * @return true if two objects are equal, false otherwise.
     * @see #compareTo(CollationKey)
     * @exception ClassCastException thrown when the specified argument is not 
     *            a CollationKey. NullPointerException thrown when argument 
     *            is null.
     * @draft 2.2
     */
    public boolean equals(Object target) 
    {
        if (!(target instanceof CollationKey)) {
            return false;
        }
        
        return equals((CollationKey)target);
    }
    
    /**
     * <p>
     * Compare this CollationKey and the argument target CollationKey for 
     * equality.
     * The collation 
     * rules of the Collator object which created these objects are applied.
     * </p>
     * <p>
     * See note in compareTo(CollationKey) for warnings of incorrect results
     * </p>
     * @param target the CollationKey to compare to.
     * @return true if two objects are equal, false otherwise.
     * @exception NullPointerException thrown when argument is null.
     * @draft 2.2
     */
    public boolean equals(CollationKey target) 
    {
        if (this == target) {
        	return true;
        }
        if (target == null) {
            return false;
        }
        CollationKey other = (CollationKey)target;
        int i = 0;
        while (true) {
        	if (m_key_[i] != other.m_key_[i]) {
        		return false;
        	}
        	if (m_key_[i] == 0) {
        		break;
        	}
        	i ++;
        }
        return true;
    }

    /**
     * <p>
     * Creates a hash code for this CollationKey. The hash value is calculated 
     * on the key itself, not the String from which the key was created. Thus 
     * if x and y are CollationKeys, then x.hashCode(x) == y.hashCode() 
     * if x.equals(y) is true. This allows language-sensitive comparison in a 
     * hash table.
     * </p>
     * @return the hash value.
     * @draft 2.2
     */
    public int hashCode() 
    {
    	if (m_hashCode_ == 0) {
    		int size = m_key_.length >> 1;
    		StringBuffer key = new StringBuffer(size);
    		int i = 0;
    		while (m_key_[i] != 0 && m_key_[i + 1] != 0) {
    			key.append((char)((m_key_[i] << 8) | m_key_[i + 1]));
    			i += 2;
    		}
    		if (m_key_[i] != 0) {
    			key.append((char)(m_key_[i] << 8));
    		}
    		m_hashCode_ = key.toString().hashCode();
    	}
        return m_hashCode_;
    }

	// protected constructor ------------------------------------------------
    
    /**
     * Protected CollationKey can only be generated by Collator objects
     * @param source string the CollationKey represents
     * @param key sort key array of bytes
     * @param size of sort key 
     * @draft 2v2
     */
    CollationKey(String source, byte key[])
    {
    	m_source_ = source;
    	m_key_ = key;
    	m_hashCode_ = 0;
    }

	// private data members -------------------------------------------------

	/**
	 * Source string this CollationKey represents
	 */	
    private String m_source_;
    /**
     * Sequence of bytes that represents the sort key
     */
    private byte m_key_[];
    /**
     * Hash code for the key
     */
    private int m_hashCode_;
}