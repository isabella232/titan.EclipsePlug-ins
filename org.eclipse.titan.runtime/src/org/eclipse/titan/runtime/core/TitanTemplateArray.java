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
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.titan.runtime.core.RecordOfMatch.answer;
import org.eclipse.titan.runtime.core.RecordOfMatch.match_function_t;
import org.eclipse.titan.runtime.core.RecordOfMatch.type_of_matching;

/**
 * @author Farkas Izabella Ingrid
 */
public class TitanTemplateArray<Tvalue extends Base_Type,Ttemplate extends Base_Template> extends Restricted_Length_Template {

	//template parameters
	private Class<Tvalue> classValue;
	private Class<Ttemplate> classTemplate;
	private int array_size;
	private int indexOffset;

	//single value
	private ArrayList<Ttemplate> single_value;
	private int singleSize;

	//value array
	private ArrayList<TitanTemplateArray<Tvalue, Ttemplate>> value_list;
	private int listSize;
	/**
	 * permutation interval 
	 */
	class Pair_of_elements {
		//beginning and ending index
		private int start_index;
		private int end_index;

		public Pair_of_elements( int start_index, int end_index ) {
			this.start_index = start_index;
			this.end_index = end_index;
		}
	};

	private ArrayList<Pair_of_elements> permutationIntervals;

	//private part
	//TODO:	void encode_text_permutation(Text_Buf& text_buf) const;
	//TODO:	void decode_text_permutation(Text_Buf& text_buf);

	private void clean_up_intervals() {
		//numberOfperm = 0;
		permutationIntervals = null;
	}

	@Override
	protected void setSelection(final template_sel otherValue) {
		super.setSelection(otherValue);
		clean_up_intervals();		
	}

	protected void setSelection(final TitanTemplateArray<Tvalue, Ttemplate> otherValue) {
		super.setSelection(otherValue); 
		clean_up_intervals();

		if (otherValue.templateSelection == template_sel.SPECIFIC_VALUE) {
			permutationIntervals = new ArrayList<Pair_of_elements>(otherValue.permutationIntervals.size());
			permutationIntervals.addAll(otherValue.permutationIntervals);
		}
	}

	public final List<Pair_of_elements> copyPermutations( final List<Pair_of_elements> srcList ) {
		if ( srcList == null ) {
			return null;
		}

		final List<Pair_of_elements> newList = new ArrayList<Pair_of_elements>( srcList.size() );
		for (Pair_of_elements srcElem : srcList) {
			Pair_of_elements newElem = new Pair_of_elements( srcElem.start_index, srcElem.start_index );
			newList.add( newElem );
		}
		return newList;
	}

	//FIXME: rename removeAllPermutations
	public void removeAllPermuations() {
		clean_up_intervals();
	}

	public void add_permutation(int start_index, int end_index) {
		if(start_index > end_index) {
			throw new TtcnError("wrong permutation interval settings start "+start_index+" can not be greater than end "+ end_index );
		}

		final int number_of_permutations = get_number_of_permutations();
		if(number_of_permutations > 0 && permutationIntervals.get( number_of_permutations - 1 ).end_index >= start_index) {
			throw new TtcnError( MessageFormat.format( "the {0}th permutation overlaps the previous one", number_of_permutations ) );
		}

		final Pair_of_elements newElem = new Pair_of_elements( start_index, end_index );
		permutationIntervals.add( newElem );
	}

	public int get_number_of_permutations() {
		return permutationIntervals != null ? permutationIntervals.size() : 0;
	}

	public int get_permutation_start(int index_value) {
		if(index_value >= get_number_of_permutations()) {
			throw new TtcnError( MessageFormat.format( "Index overflow ({0})", index_value ) );
		}

		return permutationIntervals.get(index_value).start_index;
	}

	public int get_permutation_end(int index_value) {
		if(index_value >= get_number_of_permutations()) {
			throw new TtcnError( MessageFormat.format( "Index overflow ({0})", index_value ) );
		}

		return permutationIntervals.get(index_value).end_index;
	}

	public int get_permutation_size(int index_value) {
		if(index_value >= get_number_of_permutations()) {
			throw new TtcnError( MessageFormat.format( "Index overflow ({0})", index_value ) );
		}

		return permutationIntervals.get(index_value).end_index - permutationIntervals.get(index_value).start_index + 1;
	}

	boolean permutation_starts_at(int index_value) {
		final int number_of_permutations = get_number_of_permutations();
		for(int i = 0; i < number_of_permutations; i++) {
			if(permutationIntervals.get( i ).start_index == index_value)
				return true;
		}

		return false;
	}

	boolean permutation_ends_at(int index_value) {
		final int number_of_permutations = get_number_of_permutations();
		for(int i = 0; i < number_of_permutations; i++) {
			if(permutationIntervals.get( i ).end_index == index_value)
				return true;
		}

		return false;
	}

	//FIXME: assign helper 
	private void copy_value(TitanValueArray<Tvalue> otherValue) {
		single_value = new ArrayList<Ttemplate>(otherValue.array_size);
		singleSize = otherValue.array_size;
		indexOffset = otherValue.indexOffset;

		for (int i = 0; i < array_size; ++i) {
			try {
				Ttemplate helper = classTemplate.newInstance();
				helper.assign(otherValue.getAt(i));
				single_value.add(helper);
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		setSelection(template_sel.SPECIFIC_VALUE);
	}

	//FIXME: 
	private void copy_template(final TitanTemplateArray<Tvalue, Ttemplate> otherValue) {
		switch (otherValue.templateSelection)
		{
		case SPECIFIC_VALUE:
			single_value = new ArrayList<Ttemplate>(otherValue.single_value.size());
			singleSize = otherValue.array_size;
			indexOffset = otherValue.indexOffset;
			array_size = otherValue.array_size;

			for (int i = 0; i < single_value.size(); ++i) {
				try {
					Ttemplate helper = classTemplate.newInstance();
					helper.assign(otherValue.single_value.get(i));
					single_value.add(helper);
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			break;
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			value_list = new ArrayList<TitanTemplateArray<Tvalue,Ttemplate>>();
			listSize = otherValue.array_size;
			array_size = otherValue.array_size;
			indexOffset = otherValue.indexOffset;

			for (int i = 0; i < listSize; ++i) {
				value_list.add(new TitanTemplateArray<Tvalue, Ttemplate>(classValue,classTemplate));
			}
			for (int list_count = 0; list_count < otherValue.value_list.size(); list_count++)
				value_list.get(list_count).copy_template(otherValue.value_list.get(list_count));
			break;
		default:
			throw new TtcnError("Copying an uninitialized/unsupported array template.");
		}
		setSelection(otherValue);
	}

	public TitanTemplateArray(Class<Tvalue> classValue) {
		this.classValue = classValue;
	}

	public TitanTemplateArray(Class<Tvalue> classValue, Class<Ttemplate> classTemplate) {
		this.classValue = classValue;
		this.classTemplate = classTemplate;

	}

	public TitanTemplateArray(Class<Tvalue> classValue, Class<Ttemplate> classTemplate, template_sel otherValue) {
		super(otherValue);
		checkSingleSelection(otherValue);

		this.classValue = classValue;
		this.classTemplate = classTemplate;
	}

	// originally TEMPLATE_ARRAY(null_type other_value);
	public TitanTemplateArray(Class<Tvalue> classValue, Class<Ttemplate> classTemplate,TitanNull_Type otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		this.classValue = classValue;
		this.classTemplate = classTemplate;

		single_value = null;
		permutationIntervals = null;
	}

	public TitanTemplateArray(Class<Ttemplate> classTemplate, TitanValueArray<Tvalue> otherValue) {
		this.classValue = otherValue.clazz;
		this.classTemplate = classTemplate;

		copy_value(otherValue); 
	}

	public TitanTemplateArray(TitanTemplateArray<Tvalue,Ttemplate> otherValue) {
		this.classValue = otherValue.classValue;
		this.classTemplate = otherValue.classTemplate;

		array_size = otherValue.array_size;
		indexOffset = otherValue.indexOffset;

		copy_template(otherValue); 
	}

	//TODO: TEMPLATE_ARRAY(const OPTIONAL< VALUE_ARRAY<T_value...>)

	//FIXME: actualization
	public void setSize(final int length) {
		if (length < 0) {
			throw new TtcnError("Internal error: Setting a negative size for an array template.");
		}
		template_sel old_selection = templateSelection;

		if (old_selection != template_sel.SPECIFIC_VALUE) {
			cleanUp();
			setSelection(template_sel.SPECIFIC_VALUE);
			single_value = null;
		}

		if (length > singleSize) {
			single_value = new ArrayList<Ttemplate>();
			if (old_selection == template_sel.ANY_VALUE || old_selection == template_sel.ANY_OR_OMIT) {

				for (int i = single_value.size(); i < length; ++i) {
					try {
						Ttemplate helper = classTemplate.newInstance();
						helper.setSelection(template_sel.ANY_VALUE);
						single_value.add(helper);
					} catch (InstantiationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} else {
				for (int i = singleSize; i < length; ++i) {
					try {
						Ttemplate helper = classTemplate.newInstance();
						single_value.add(helper);
					} catch (InstantiationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			//single_value.n_elements = length;
		} else if (length < singleSize) {
			for (int i = length; i < singleSize; ++i) {
				single_value.remove(i);
			}
		}

		array_size = length;
		singleSize = length;
	}

	public void setOffset(final int offset) {
		indexOffset = offset;
	}

	public void cleanUp() {
		switch (templateSelection)
		{
		case SPECIFIC_VALUE:
			singleSize = 0;
			single_value.clear();
			single_value = null;
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			listSize = 0;
			for (int i = 0; i < value_list.size(); ++i) {
				value_list.get(i).cleanUp();
			}
			value_list = null;
			break;
		default:
			break;
		}
		templateSelection = template_sel.UNINITIALIZED_TEMPLATE;
	}

	// originally operator=
	public TitanTemplateArray<Tvalue, Ttemplate> assign(final template_sel otherValue) {
		checkSingleSelection(otherValue);
		cleanUp();
		setSelection(otherValue);
		return this;
	}

	//FIXME: originally operator=(null_type)
	public TitanTemplateArray<Tvalue, Ttemplate> assign(final TitanNull_Type otherValue) {
		cleanUp();
		setSelection(template_sel.SPECIFIC_VALUE);
		return this;
	}

	// originally operator=
	public TitanTemplateArray<Tvalue, Ttemplate> assign(final TitanValueArray<Tvalue> otherValue) {
		cleanUp();
		copy_value(otherValue);
		return this;
	}

	//TODO: operator=(Optional...)

	// originally operator=
	public TitanTemplateArray<Tvalue, Ttemplate> assign(final TitanTemplateArray<Tvalue,Ttemplate> otherValue) {
		if (otherValue != this) {
			cleanUp();
			copy_template(otherValue);
		}

		return this;
	}

	// originally T& operator[](int)
	public Ttemplate getAt(int index) {
		if (index < indexOffset || index >= indexOffset + array_size) {
			throw new TtcnError(MessageFormat.format("Accessing an element of an array template using invalid index: {0}. "
					+ "Index range is [{1},{2}].",
					index, indexOffset, indexOffset+(int)array_size));
		}
		index -= indexOffset;
		switch (templateSelection) {
		case SPECIFIC_VALUE:
			if (index >= single_value.size()) setSize(index + 1);
			break;
		case ANY_VALUE:
		case ANY_OR_OMIT:
			setSize(value_list.size());
			break; 
		default:
			setSize(index + 1);
		}
		return single_value.get(index);
	}

	//originally T& operator[](const INTEGER)
	public Ttemplate getAt(final TitanInteger index) {
		index.mustBound("Using an unbound integer value for indexing an array template.");
		return getAt(index.getInt());
	}

	// originally const T& operator[](int)
	public Ttemplate constGetAt(int index) {
		if (index < indexOffset ) {
			throw new TtcnError(MessageFormat.format("Accessing an element of an array template using invalid index: {0}. "
					+ "Index range is [{1},{2}].", index, indexOffset, indexOffset+(int)array_size));
		}
		index -= indexOffset;
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Accessing an element of a non-specific array template.");
		}
		if (index >= single_value.size()) {
			throw new TtcnError(MessageFormat.format("Index overflow in an array template: The index is {0} (starting at {1}),"
					+ " but the template has only {2} elements.",index +indexOffset, indexOffset, single_value.size()));
		}

		return single_value.get(index);
	}

	// originally T& operator[](const INTEGER)
	public Ttemplate constGetAt(final TitanInteger index) {
		index.mustBound("Using an unbound integer value for indexing an array template.");
		return constGetAt(index.getInt());	
	}

	public int nofElements() {
		switch (templateSelection) {
		case SPECIFIC_VALUE:
			return single_value.size();
		case VALUE_LIST:
			return value_list.size();
		default:
			throw new TtcnError("Performing n_elem");
		}
	}

	public TitanInteger lengthOf() {
		return sizeOf(false);
	}

	public TitanInteger sizeOf(){
		return sizeOf(true);
	}

	// originally size_of
	public TitanInteger sizeOf(boolean isSize) {
		final String opName = isSize ? "size" : "length";
		int minSize = 0;
		boolean has_any_or_none = false;

		if (is_ifPresent) {
			throw new TtcnError("Performing "+opName+"of() operation on an array template which has an ifpresent attribute.");
		}

		switch (templateSelection) {
		case SPECIFIC_VALUE:
			minSize = 0;
			has_any_or_none = false;
			int count = single_value.size();
			if (! isSize) { //lengthof()
				while (count > 0 && !single_value.get(count-1).isBound().getValue()) {
					count -=1;
				}
			}

			for (int i = 0; i < count; ++i) {
				switch (single_value.get(i).getSelection()) {
				case OMIT_VALUE:
					throw new TtcnError("Performing"+opName+"of() operation on an array template containing omit element.");
				case ANY_OR_OMIT:
					has_any_or_none = true;
					break;
				default:
					minSize +=1;
					break;
				}
			}
			break;
		case OMIT_VALUE:
			throw new TtcnError("Performing "+opName+"of() operation on an array template containing omit element.");
		case ANY_VALUE:
		case ANY_OR_OMIT:
			minSize = 0;
			has_any_or_none = true;
			break;
		case VALUE_LIST:
			if (value_list.size() < 1) {
				throw new TtcnError("Performing "+opName+"of() operation on an array template containing an empty list.");
			}
			int itemSize = value_list.get(0).sizeOf(isSize).getInt();
			for (int i = 1; i < value_list.size(); ++i) {
				if (value_list.get(i).sizeOf(isSize).getInt() != itemSize) {
					throw new TtcnError("Performing "+opName+"of() operation on an array template containing a value list with different sizes.");
				}
			}
			minSize = itemSize;
			has_any_or_none = false;
			break;
		case COMPLEMENTED_LIST:
			throw new TtcnError("Performing "+opName+"of() operation on an array template containing complemented list.");
		default:
			throw new TtcnError("Performing "+opName+"of() operation on an uninitialized/unsupported array template.");
		}

		return new TitanInteger(check_section_is_single(minSize, has_any_or_none, opName, "an", "array template"));
	}

	@Override
	public TitanBoolean isValue() {
		if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			return new TitanBoolean(false);
		}

		for (int i = 0; i < single_value.size(); ++i) {
			if (! single_value.get(i).isValue().getValue()) {
				return new TitanBoolean(false);
			}
		}

		return new TitanBoolean(true);
	}

	//FIXME: initialized result 
	public TitanValueArray<Tvalue> valueOf() {
		if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent)
			throw new TtcnError("Performing a valueof or send operation on a non-specific array template.");
		// the size of the template must be the size of the value
		if (single_value.size() != array_size)
			throw new TtcnError("Performing a valueof or send operation on a specific array template with invalid size.");
		TitanValueArray<Tvalue> result = null;
		for (int i = 0; i < array_size; ++i) {
			//result.array_elements.add(single_value.get(i).valueOf());
		}
		// ret_val.array_element(elem_count) = single_value.get(elem_count).valueOf();
		return result;
	}

	public void setType(template_sel templateType, int length) {
		cleanUp();
		switch (templateType) {
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			listSize = length;
			value_list = new ArrayList<TitanTemplateArray<Tvalue, Ttemplate>>(length);
			for (int i = 0; i < length; ++i) {
				value_list.add(new TitanTemplateArray<Tvalue,Ttemplate>(classValue, classTemplate));
			}

			break;
		default:
			throw new TtcnError("Internal error: Setting an invalid type for an array template.");
		}
		setSelection(templateType);
	}

	public TitanTemplateArray<Tvalue, Ttemplate> listItem(int index) {
		if (templateSelection != template_sel.VALUE_LIST &&
				templateSelection != template_sel.COMPLEMENTED_LIST)
			throw new TtcnError("Internal error: Accessing a list element of a non-list array template.");
		if (index >= value_list.size()) {
			throw new TtcnError("Internal error: Index overflow in a value list array template.");
		}
		if (index < 0 ) {
			throw new TtcnError("Internal error: Index overflow in a value list array template.");
		}
		return value_list.get(index);
	}

	@SuppressWarnings("unchecked")
	private TitanBoolean match_function_specific(Base_Type value, int valueIndex, Restricted_Length_Template template, int templateIndex, boolean legacy) {
		if (valueIndex >= 0) {
			return ((TitanTemplateArray<Tvalue, Ttemplate>)template).single_value.get(templateIndex)
					.match(((TitanValueArray<Tvalue>) value).array_elements.get(valueIndex), legacy);
		} else {
			return new TitanBoolean(((TitanTemplateArray<Tvalue, Ttemplate>)template).single_value.get(templateIndex).is_any_or_omit());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public TitanBoolean match(Base_Type otherValue, boolean legacy) {
		if (otherValue instanceof TitanValueArray<?>) {
			return match((TitanValueArray<Tvalue>) otherValue, legacy);
		}
		throw new TtcnError(MessageFormat.format("Internal Error: value {0} can not be cast to value array", otherValue));
	}

	public TitanBoolean match(final TitanValueArray<Tvalue> otherValue) {
		return match(otherValue,false);
	}

	public TitanBoolean match(final TitanValueArray<Tvalue> otherValue, boolean legacy ) {
		if (!match_length(array_size)) return new TitanBoolean(false);
		switch (templateSelection)
		{
		case SPECIFIC_VALUE:
			match_function_t obj = new match_function_t() {

				@Override
				public boolean match(Base_Type value_ptr, int value_index,
						Restricted_Length_Template template_ptr, int template_index,
						boolean legacy) {
					return match_function_specific(value_ptr, value_index, template_ptr, template_index, legacy).getValue();
				}
			};
			return new TitanBoolean(match_permutation_array(otherValue, array_size, this, single_value.size(),obj, legacy));
		case OMIT_VALUE:
			return new TitanBoolean(false);
		case ANY_VALUE:
		case ANY_OR_OMIT:
			return new TitanBoolean(true);
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			for (int i = 0; i < value_list.size(); i++)
				if (value_list.get(i).match(otherValue, legacy).getValue()) {
					return new TitanBoolean(templateSelection == template_sel.VALUE_LIST);
				}
			return new TitanBoolean(templateSelection == template_sel.COMPLEMENTED_LIST);
		default:
			throw new TtcnError("Matching with an uninitialized/unsupported array template.");
		}
	}

	public TitanBoolean isPresent() {
		return isPresent(false);
	}

	public TitanBoolean isPresent(boolean legacy) {
		if (templateSelection == template_sel.UNINITIALIZED_TEMPLATE) {
			return new TitanBoolean(false);
		}

		return new TitanBoolean(true);
	}

	public TitanBoolean match_omit() {
		return match_omit(false);
	}

	public TitanBoolean match_omit(boolean legacy) {
		if (is_ifPresent) {
			return new TitanBoolean(true);
		}
		switch (templateSelection) {
		case OMIT_VALUE:
		case ANY_OR_OMIT:
			return new TitanBoolean(true);
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			if (legacy) {
				// legacy behavior: 'omit' can appear in the value/complement list
				for (int i=0; i<value_list.size(); i++) {
					if (value_list.get(i).match_omit().getValue()) {
						return new TitanBoolean(templateSelection==template_sel.VALUE_LIST);
					}
				}

				return new TitanBoolean(templateSelection==template_sel.COMPLEMENTED_LIST);
			}
			// else fall through
		default:
			return new TitanBoolean(false);
		}
	}

	@Override
	public TitanTemplateArray<Tvalue, Ttemplate> assign(Base_Type otherValue) {
		if (otherValue instanceof TitanValueArray<?>) {
			final TitanValueArray<?> arrayOther = (TitanValueArray<?>)otherValue;
			return assign(arrayOther);
		} else {
			try {
				Ttemplate value = classTemplate.newInstance();
				value.assign(otherValue);
				single_value = new ArrayList<Ttemplate>();
				single_value.add(value);
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return this;
		}
	}

	@Override
	public TitanTemplateArray<Tvalue, Ttemplate> assign(Base_Template otherValue) {
		if (otherValue instanceof TitanTemplateArray<?,?>) {
			final TitanTemplateArray<?,?> arrayOther = (TitanTemplateArray<?,?>)otherValue;
			return assign(arrayOther);
		} else {
			try {
				Ttemplate value = classTemplate.newInstance();
				value.assign(otherValue);
				single_value = new ArrayList<Ttemplate>();
				single_value.add(value);
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return this;
		}
	}

	private answer recursive_permutation_match(final Base_Type value_ptr,
			int value_start_index,
			int value_size,
			final TitanTemplateArray<Tvalue, Ttemplate> template_ptr,
			int template_start_index,
			int template_size,
			int permutation_index,
			match_function_t match_function,
			AtomicInteger shift_size,
			boolean legacy) {

		int nof_permutations = template_ptr.get_number_of_permutations();
		if (permutation_index > nof_permutations) {
			throw new TtcnError("Internal error: recursive_permutation_match: invalid argument.");
		}

		if (permutation_index < nof_permutations && template_ptr.get_permutation_end(permutation_index) > template_start_index + template_size) {
			throw new TtcnError(MessageFormat.format( "Internal error: recursive_permutation_match: wrong permutation interval settings for permutation {0}.", permutation_index));
		}

		shift_size.set( 0 );

		//trivial cases
		if(template_size == 0) {
			//reached the end of templates
			// if we reached the end of values => good
			// else => bad
			if(value_size == 0) {
				return answer.SUCCESS;
			} else {
				return answer.FAILURE;
			}
		}

		//are we at an asterisk or at the beginning of a permutation interval
		boolean is_asterisk;
		boolean permutation_begins = permutation_index < nof_permutations &&
				template_start_index == template_ptr.get_permutation_start(permutation_index);

		if (permutation_begins ||
				match_function.match(value_ptr, -1, template_ptr, template_start_index, legacy)) {
			int smallest_possible_size;
			int largest_possible_size;
			boolean has_asterisk;
			boolean already_superset;
			int permutation_size;

			//check how many values might be associated with this permutation
			//if we are at a permutation start
			if (permutation_begins) {
				is_asterisk = false;
				permutation_size = template_ptr.get_permutation_size(permutation_index);
				smallest_possible_size = 0;
				has_asterisk = false;

				//count how many non asterisk elements are in the permutation
				for(int i = 0; i < permutation_size; i++) {
					if(match_function.match(value_ptr, -1, template_ptr, i + template_start_index, legacy)) {
						has_asterisk = true;
					} else {
						smallest_possible_size++;
					}
				}

				//the real permutation size is bigger then the value size
				if(smallest_possible_size > value_size) {
					return answer.NO_CHANCE;
				}

				//if the permutation has an asterisk then it can grow
				if(has_asterisk) {
					largest_possible_size = value_size;

					//if there are only asterisks in the permutation
					if(smallest_possible_size == 0) {
						already_superset = true;
					} else {
						already_superset = false;
					}
				} else {
					//without asterisks its size is fixed
					largest_possible_size = smallest_possible_size;
					already_superset = false;
				}
			} else {
				//or at an asterisk
				is_asterisk = true;
				already_superset = true;
				permutation_size = 1;
				smallest_possible_size = 0;
				largest_possible_size = value_size;
				has_asterisk = true;
			}

			int temp_size = smallest_possible_size;

			{
				//this is to make match_set_of incremental,
				// we store the already found pairs in this vector
				// so we wouldn't try to find a pair for those templates again
				// and we can set the covered state of values too
				// to not waste memory it is only created if needed
				int[] pair_list = null;
				int old_temp_size = 0;

				if(!already_superset) {
					pair_list = new int[permutation_size];
					for(int i = 0 ; i < permutation_size; i++) {
						//in the beginning we haven't found a template to any values
						pair_list[i] = -1;
					}
				}

				while(!already_superset) {
					//must be a permutation having other values than asterisks

					AtomicInteger x = new AtomicInteger(0);

					//our set matching is extended with 2 more parameters
					// giving back how many templates
					// (other than asterisk) couldn't be matched
					// and setting / giving back the value-template pairs

					boolean found = RecordOfMatch.match_set_of_internal(value_ptr, value_start_index,
							temp_size, template_ptr,
							template_start_index, permutation_size,
							match_function, type_of_matching.SUPERSET, x, pair_list,old_temp_size, legacy);

					if(found) {
						already_superset = true;
					} else {
						//as we didn't found a match we have to try
						// a larger set of values
						//x is the number of templates we couldn't find
						// a matching pair for
						// the next must be at least this big to fully cover
						// on the other side if it would be bigger than it might miss
						// the smallest possible match.

						//if we can match with more values
						if(has_asterisk && temp_size + x.get() <= largest_possible_size) {
							old_temp_size = temp_size;
							temp_size += x.get();
						} else {
							return answer.FAILURE; //else we failed
						}
					}
				}
			}

			//we reach here only if we found a match

			//can only go on recursively if we haven't reached the end

			//reached the end of templates
			if(permutation_size == template_size) {
				if(has_asterisk || value_size == temp_size) {
					return answer.SUCCESS;
				} else {
					return answer.FAILURE;
				}
			}

			for(int i = temp_size; i <= largest_possible_size;) {
				answer result;

				if(is_asterisk) {
					//don't step the permutation index
					result = recursive_permutation_match(value_ptr,value_start_index+i,
							value_size - i, template_ptr,
							template_start_index + permutation_size,
							template_size - permutation_size,
							permutation_index,
							match_function, shift_size, legacy);
				} else {
					//try with the next permutation
					result = recursive_permutation_match(value_ptr,value_start_index+i,
							value_size - i, template_ptr,
							template_start_index + permutation_size,
							template_size - permutation_size,
							permutation_index + 1,
							match_function, shift_size, legacy);
				}

				if(result == answer.SUCCESS) {
					//we finished
					return answer.SUCCESS;
				} else if(result == answer.NO_CHANCE) {
					//matching is not possible
					return answer.NO_CHANCE;
				} else if(i == value_size) {
					//we failed
					//if there is no chance of matching
					return answer.NO_CHANCE;
				} else {
					i += shift_size.get() > 1 ? shift_size.get() : 1;

					if(i > largest_possible_size) {
						shift_size.set( i - largest_possible_size );
					} else {
						shift_size.set( 0 );
					}
				}
			}

			//this level failed;
			return answer.FAILURE;
		} else {
			//we are at the beginning of a non permutation, non asterisk interval

			//the distance to the next permutation or the end of templates
			// so the longest possible match
			int distance;

			if (permutation_index < nof_permutations) {
				distance = template_ptr.get_permutation_start(permutation_index) - template_start_index;
			} else {
				distance = template_size;
			}

			//if there are no more values, but we still have templates
			// and the template is not an asterisk or a permutation start
			if(value_size == 0) {
				return answer.FAILURE;
			}

			//we try to match as many values as possible
			//an asterisk is handled like a 0 length permutation
			boolean good;
			int i = 0;
			do {
				good = match_function.match(value_ptr, value_start_index + i,
						template_ptr, template_start_index + i, legacy);
				i++;
				//bad stop: something can't be matched
				//half bad half good stop: the end of values is reached
				//good stop: matching on the full distance or till an asterisk
			} while(good && i < value_size && i < distance &&
					!match_function.match(value_ptr, -1, template_ptr, template_start_index + i, legacy));

			//if we matched on the full distance or till an asterisk
			if(good && (i == distance ||
					match_function.match(value_ptr, -1, template_ptr, template_start_index + i, legacy))) {
				//reached the end of the templates
				if ( i == template_size ) {
					if (i < value_size ) {
						//the next level would return FAILURE so we don't step it
						return answer.FAILURE;
					} else {
						//i == value_size, so we matched everything
						return answer.SUCCESS;
					}
				} else {
					//we reached the next asterisk or permutation,
					// so step to the next level
					return recursive_permutation_match(value_ptr,value_start_index + i,
							value_size - i,
							template_ptr,
							template_start_index + i,
							template_size - i,
							permutation_index,
							match_function, shift_size, legacy);
				}
			} else {
				//something bad happened, so we have to check how bad the situation is
				if ( i == value_size ) {
					//the aren't values left, meaning that the match is not possible
					return answer.NO_CHANCE;
				} else {
					//we couldn't match, but there is still a chance of matching

					//try to find a matching value for the last checked (and failed)
					// template.
					// smaller jumps would fail so we skip them
					shift_size.set( 0 );
					i--;
					do {
						good = match_function.match(value_ptr, value_start_index + i + shift_size.get(), template_ptr, template_start_index + i, legacy);
						shift_size.incrementAndGet();
					} while(!good && i + shift_size.get() < value_size);

					if(good) {
						shift_size.decrementAndGet();
						return answer.FAILURE;
					} else {
						// the template can not be matched later
						return answer.NO_CHANCE;
					}
				}
			}
		}
	}

	boolean match_permutation_array(final Base_Type value_ptr, int value_size, 
			final TitanTemplateArray<Tvalue, Ttemplate> template_ptr, int template_size,
			match_function_t match_function, boolean legacy) {

		if (value_ptr == null || value_size < 0 ||
				template_ptr == null || template_size < 0 ||
				template_ptr.getSelection() != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Internal error: match_permutation_arry: invalid argument.");
		}

		int nof_permutations = template_ptr.get_number_of_permutations();

		// use the simplified algorithm if the template does not contain permutation
		if (nof_permutations == 0) {
			return RecordOfMatch.match_array(value_ptr, value_size,
					template_ptr, template_size, match_function, legacy);
		}

		// use 'set of' matching if all template elements are grouped into one permutation
		if (nof_permutations == 1 && template_ptr.get_permutation_start(0) == 0 &&
				template_ptr.get_permutation_end(0) == (template_size - 1)) {
			return RecordOfMatch.match_set_of(value_ptr, value_size, template_ptr, template_size, match_function, legacy);
		}

		AtomicInteger shift_size = new AtomicInteger( 0 );
		return recursive_permutation_match(value_ptr, 0, value_size, template_ptr,
				0, template_size, 0, match_function, shift_size, legacy) == answer.SUCCESS;
	}

}
