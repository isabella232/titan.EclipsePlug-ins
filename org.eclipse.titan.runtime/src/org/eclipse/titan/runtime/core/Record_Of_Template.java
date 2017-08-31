/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Record_Of_Template in titan.core
 * 
 * @author Arpad Lovassy
 */
public abstract class Record_Of_Template extends Restricted_Length_Template {

	/**
	 * permutation interval 
	 */
	class Pair_of_elements {
		//beginning and ending index
		private int start_index;
		private int end_index;

		public Pair_of_elements( final int start_index, final int end_index ) {
			this.start_index = start_index;
			this.end_index = end_index;
		}
	};

	List<Pair_of_elements> permutation_intervals;

	public Record_Of_Template() {
		permutation_intervals = null;
	}

	public Record_Of_Template(final template_sel other_value) {
		super(other_value);
		permutation_intervals = null;
	}

	void clean_up_intervals() {
		permutation_intervals = null;
	}

	@Override
	protected void setSelection(final template_sel other_value) {
		super.setSelection(other_value);
		clean_up_intervals();
	}

	void setSelection(final Record_Of_Template other_value) {
		super.setSelection(other_value);
		clean_up_intervals();
		if(other_value.templateSelection == template_sel.SPECIFIC_VALUE) {
			permutation_intervals = copyPermutations( other_value.permutation_intervals );
		}
	}

	public final List<Pair_of_elements> copyPermutations( final List<Pair_of_elements> srcList ) {
		if ( srcList == null ) {
			return null;
		}

		final List<Pair_of_elements> newList = new ArrayList<Pair_of_elements>( srcList.size() );
		for (Pair_of_elements srcElem : srcList) {
			final Pair_of_elements newElem = new Pair_of_elements( srcElem.start_index, srcElem.start_index );
			newList.add( newElem );
		}
		return newList;
	}

	//TODO: implement void encode_text_permutation(Text_Buf text_buf)
	//TODO: implement void decode_text_permutation(Text_Buf text_buf)
	/*
	void encode_text_permutation(Text_Buf text_buf) {
		encode_text_restricted(text_buf);
		final int number_of_permutations = get_number_of_permutations();
		text_buf.push_int(number_of_permutations);

		for(int i = 0; i < number_of_permutations; i++) {
			text_buf.push_int(permutation_intervals.get( i ).start_index);
			text_buf.push_int(permutation_intervals.get( i ).end_index);
		}
	}

	void decode_text_permutation(Text_Buf text_buf) {
		decode_text_restricted(text_buf);

		final int number_of_permutations = text_buf.pull_int().get_val();
		permutation_intervals = new ArrayList<Pair_of_elements>(number_of_permutations);

		for (int i = 0; i < number_of_permutations; i++) {
			final int start_index = text_buf.pull_int().get_val();
			final int end_index = text_buf.pull_int().get_val();
			permutation_intervals.add( new Pair_of_elements( start_index, end_index ) );
		}
	}
	*/

	void add_permutation(final int start_index, final int end_index) {
		if(start_index > end_index) {
			throw new TtcnError( MessageFormat.format( "wrong permutation interval settings start ({0})can not be greater than end ({1})",start_index, end_index ) );
		}

		final int number_of_permutations = get_number_of_permutations();
		if(number_of_permutations > 0 &&
				permutation_intervals.get( number_of_permutations - 1 ).end_index >= start_index) {
			//TODO: fix {0}th (also in titan.core), it can be 1st, 2nd, 3rd, but 11th, 12th, 13th
			//throw new TtcnError( MessageFormat.format( "the {0}{1} permutation overlaps the previous one", number_of_permutations, getOrdinalIndicator(number_of_permutations) ) );
			throw new TtcnError( MessageFormat.format( "the {0}th permutation overlaps the previous one", number_of_permutations ) );
		}

		final Pair_of_elements newElem = new Pair_of_elements( start_index, end_index );
		permutation_intervals.add( newElem );
	}

	//TODO: move it to a utility class
	/**
	 * Calculates the ordinal indicator for an integer number, like 5th, it can be 1st, 2nd, 3rd, but 11th, 12th, 13th
	 * @param n integer number
	 * @return st, nd, rd or th
	 */
	private static String getOrdinalIndicator( final int n ) {
		if ( 11 <= n % 100 && n % 100 <= 13 ) {
			//exception case
			return "th";
		}
		switch ( n % 10 ) {
		case 1:
			return "st";
		case 2:
			return "nd";
		case 3:
			return "rd";
		default:
			return "th";
		}
	}

	public int get_number_of_permutations() {
		return permutation_intervals != null ? permutation_intervals.size() : 0;
	}

	public int get_permutation_start(final int index_value) {
		if(index_value >= get_number_of_permutations()) {
			throw new TtcnError( MessageFormat.format( "Index overflow ({0})", index_value ) );
		}

		return permutation_intervals.get(index_value).start_index;
	}

	public int get_permutation_end(final int index_value) {
		if(index_value >= get_number_of_permutations()) {
			throw new TtcnError( MessageFormat.format( "Index overflow ({0})", index_value ) );
		}

		return permutation_intervals.get(index_value).end_index;
	}

	public int get_permutation_size(final int index_value) {
		if(index_value >= get_number_of_permutations()) {
			throw new TtcnError( MessageFormat.format( "Index overflow ({0})", index_value ) );
		}

		return permutation_intervals.get(index_value).end_index - permutation_intervals.get(index_value).start_index + 1;
	}

	boolean permutation_starts_at(final int index_value) {
		final int number_of_permutations = get_number_of_permutations();
		for(int i = 0; i < number_of_permutations; i++) {
			if(permutation_intervals.get( i ).start_index == index_value)
				return true;
		}

		return false;
	}

	boolean permutation_ends_at(final int index_value) {
		final int number_of_permutations = get_number_of_permutations();
		for(int i = 0; i < number_of_permutations; i++) {
			if(permutation_intervals.get( i ).end_index == index_value)
				return true;
		}

		return false;
	}
}
