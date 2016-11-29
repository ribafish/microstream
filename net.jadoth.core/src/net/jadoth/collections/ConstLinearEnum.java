package net.jadoth.collections;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.jadoth.Jadoth;
import net.jadoth.collections.old.AbstractOldGettingSet;
import net.jadoth.collections.types.IdentityEqualityLogic;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.collections.types.XImmutableEnum;
import net.jadoth.exceptions.IndexBoundsException;
import net.jadoth.functional.BiProcedure;
import net.jadoth.functional.IndexProcedure;
import net.jadoth.functional.JadothEqualators;
import net.jadoth.util.Equalator;
import net.jadoth.util.iterables.ReadOnlyListIterator;


/**
 *
 * @author Thomas Muenz
 * @version 0.91, 2011-02-28
 */
public final class ConstLinearEnum<E> extends AbstractSimpleArrayCollection<E>
implements XImmutableEnum<E>, IdentityEqualityLogic
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	private static final Object[] EMPTY_DATA = new Object[0];



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Object[] data; // the storage array containing the elements



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ConstLinearEnum() // required for "empty" list constant
	{
		super();
		this.data = EMPTY_DATA;
	}

	public ConstLinearEnum(final int initialCapacity)
	{
		super();
		this.data = new Object[initialCapacity];
	}

	public ConstLinearEnum(final ConstLinearEnum<? extends E> original) throws NullPointerException
	{
		super();
		this.data = original.data.clone();
	}

	public ConstLinearEnum(final XGettingCollection<? extends E> elements) throws NullPointerException
	{
		super();
		this.data = elements.toArray();
	}

	@SafeVarargs
	public ConstLinearEnum(final E... elements) throws NullPointerException
	{
		super();
		this.data = elements.clone();
	}

	public ConstLinearEnum(final E[] src, final int srcStart, final int srcLength)
	{
		super();
		// automatically check arguments 8-)
		System.arraycopy(src, srcStart, this.data = new Object[srcLength], 0, srcLength);
	}

	ConstLinearEnum(final Object[] internalData, final int size)
	{
		super();
		this.data = internalData;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@SuppressWarnings("unchecked")
	@Override
	protected E[] internalGetStorageArray()
	{
		return (E[])this.data;
	}

	@Override
	protected int internalSize()
	{
		return this.data.length;
	}

	@Override
	protected int[] internalGetSectionIndices()
	{
		return new int[]{0, this.data.length}; // trivial section
	}

	@Override
	public Equalator<? super E> equality()
	{
		return JadothEqualators.identity();
	}

	@Override
	protected int internalCountingAddAll(final E[] elements) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException(); // not supported
	}

	@Override
	protected int internalCountingAddAll(final E[] elements, final int offset, final int length)
		throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException(); // not supported
	}

	@Override
	protected int internalCountingAddAll(final XGettingCollection<? extends E> elements)
		throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException(); // not supported
	}

	@Override
	protected int internalCountingPutAll(final E[] elements) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException(); // not supported
	}

	@Override
	protected int internalCountingPutAll(final E[] elements, final int offset, final int length)
		throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException(); // not supported
	}

	@Override
	protected int internalCountingPutAll(final XGettingCollection<? extends E> elements)
		throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException(); // not supported
	}



	///////////////////////////////////////////////////////////////////////////
	// getting methods  //
	/////////////////////

	@Override
	public ConstLinearEnum<E> copy()
	{
		return new ConstLinearEnum<>(this);
	}

	@Override
	public ConstLinearEnum<E> immure()
	{
		return this;
	}

	@Override
	public ConstLinearEnum<E> toReversed()
	{
		final Object[] data = this.data;
		final Object[] rData = new Object[data.length];
		for(int i = data.length, r = 0; i-- > 0;)
		{
			rData[r++] = data[i];
		}
		return new ConstLinearEnum<>(rData, data.length);
	}

	@Override
	public E[] toArray(final Class<E> type)
	{
		final E[] array = JadothArrays.newArray(type, this.data.length);
		System.arraycopy(this.data, 0, array, 0, this.data.length);
		return array;
	}

	// executing //

	@SuppressWarnings("unchecked")
	@Override
	public final <P extends Consumer<? super E>> P iterate(final P procedure)
	{
		AbstractArrayStorage.iterate((E[])this.data, this.data.length, procedure);
		return procedure;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <P extends IndexProcedure<? super E>> P iterate(final P procedure)
	{
		AbstractArrayStorage.iterate((E[])this.data, this.data.length, procedure);
		return procedure;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <A> A join(final BiProcedure<? super E, ? super A> joiner, final A aggregate)
	{
		AbstractArrayStorage.join((E[])this.data, this.data.length, joiner, aggregate);
		return aggregate;
	}

	// count querying //

	@SuppressWarnings("unchecked")
	@Override
	public long count(final E element)
	{
		return AbstractArrayStorage.count((E[])this.data, this.data.length, element);
	}

	@SuppressWarnings("unchecked")
	@Override
	public long countBy(final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.conditionalCount((E[])this.data, this.data.length, predicate);
	}

	// index querying //

	@SuppressWarnings("unchecked")
	@Override
	public long indexOf(final E element)
	{
		return AbstractArrayStorage.indexOf((E[])this.data, this.data.length, element);
	}

	@SuppressWarnings("unchecked")
	@Override
	public long indexBy(final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.conditionalIndexOf((E[])this.data, this.data.length, predicate);
	}

	@SuppressWarnings("unchecked")
	@Override
	public long lastIndexOf(final E element)
	{
		return AbstractArrayStorage.rngIndexOF((E[])this.data, this.data.length, this.data.length - 1, -this.data.length, element);
	}

	@SuppressWarnings("unchecked")
	@Override
	public long lastIndexBy(final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.lastIndexOf((E[])this.data, this.data.length, predicate);
	}

	@SuppressWarnings("unchecked")
	@Override
	public long maxIndex(final Comparator<? super E> comparator)
	{
		return AbstractArrayStorage.maxIndex((E[])this.data, this.data.length, comparator);
	}

	@SuppressWarnings("unchecked")
	@Override
	public long minIndex(final Comparator<? super E> comparator)
	{
		return AbstractArrayStorage.minIndex((E[])this.data, this.data.length, comparator);
	}

	@SuppressWarnings("unchecked")
	@Override
	public long scan(final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.scan((E[])this.data, this.data.length, predicate);
	}

	// element querying //

	@SuppressWarnings("unchecked")
	@Override
	public E get()
	{
		return (E)this.data[0];
	}

	@SuppressWarnings("unchecked")
	@Override
	public E first()
	{
		return (E)this.data[0];
	}

	@SuppressWarnings("unchecked")
	@Override
	public E last()
	{
		return (E)this.data[this.data.length - 1];
	}

	@SuppressWarnings("unchecked")
	@Override
	public E poll()
	{
		return this.data.length == 0 ? null : (E)this.data[0];
	}

	@SuppressWarnings("unchecked")
	@Override
	public E peek()
	{
		return this.data.length == 0 ? null : (E)this.data[this.data.length - 1];
	}

	@SuppressWarnings("unchecked")
	@Override
	public E seek(final E sample)
	{
		return AbstractArrayStorage.containsSame((E[])this.data, this.data.length, sample) ? sample : null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public E search(final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.queryElement((E[])this.data, this.data.length, predicate, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public E max(final Comparator<? super E> comparator)
	{
		return AbstractArrayStorage.max((E[])this.data, this.data.length, comparator);
	}

	@SuppressWarnings("unchecked")
	@Override
	public E min(final Comparator<? super E> comparator)
	{
		return AbstractArrayStorage.min((E[])this.data, this.data.length, comparator);
	}

	// boolean querying //

	@Override
	public boolean hasVolatileElements()
	{
		return false;
	}

	@Override
	public boolean nullAllowed()
	{
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean isSorted(final Comparator<? super E> comparator)
	{
		return AbstractArrayStorage.isSorted((E[])this.data, this.data.length, comparator);
	}

//	@SuppressWarnings("unchecked")
//	@Override
//	public boolean hasDistinctValues()
//	{
//		return AbstractArrayStorage.hasDistinctValues((E[])this.data, this.data.length);
//	}
//
//	@SuppressWarnings("unchecked")
//	@Override
//	public boolean hasDistinctValues(final Equalator<? super E> equalator)
//	{
//		return AbstractArrayStorage.hasDistinctValues((E[])this.data, this.data.length, equalator);
//	}

	// boolean querying - applies //

	@SuppressWarnings("unchecked")
	@Override
	public boolean containsSearched(final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.contains((E[])this.data, this.data.length, predicate);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean applies(final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.applies((E[])this.data, this.data.length, predicate);
	}

	// boolean querying - contains //

	@SuppressWarnings("unchecked")
	@Override
	public boolean nullContained()
	{
		return AbstractArrayStorage.nullContained((E[])this.data, this.data.length);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean containsId(final E element)
	{
		return AbstractArrayStorage.containsSame((E[])this.data, this.data.length, element);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean contains(final E element)
	{
		return AbstractArrayStorage.containsSame((E[])this.data, this.data.length, element);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean containsAll(final XGettingCollection<? extends E> elements)
	{
		return AbstractArrayStorage.containsAll((E[])this.data, this.data.length, elements);
	}

//	@SuppressWarnings("unchecked")
//	@Override
//	public boolean containsAll(final XGettingCollection<? extends E> elements, final Equalator<? super E> equalator)
//	{
//		return AbstractArrayStorage.containsAll((E[])this.data, this.data.length, elements, equalator);
//	}

	// boolean querying - equality //

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		if(samples == this)
		{
			return true;
		}
		if(samples == null || !(samples instanceof ConstLinearEnum<?>) || Jadoth.to_int(samples.size()) != this.data.length)
		{
			return false;
		}

		// equivalent to equalsContent()
		return JadothArrays.equals(this.data, 0, ((ConstLinearEnum<?>)samples).data, 0, this.data.length, (Equalator<Object>)equalator);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equalsContent(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		if(samples == null || Jadoth.to_int(samples.size()) != this.data.length)
		{
			return false;
		}
		if(samples == this)
		{
			return true;
		}
		return AbstractArrayStorage.equalsContent((E[])this.data, this.data.length, samples, equalator);
	}

	// data set procedures //

	@SuppressWarnings("unchecked")
	@Override
	public <C extends Consumer<? super E>> C intersect(
		final XGettingCollection<? extends E> samples,
		final Equalator<? super E> equalator,
		final C target
	)
	{
		return AbstractArrayStorage.intersect((E[])this.data, this.data.length, samples, equalator, target);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <C extends Consumer<? super E>> C except(
		final XGettingCollection<? extends E> samples,
		final Equalator<? super E> equalator,
		final C target
	)
	{
		return AbstractArrayStorage.except((E[])this.data, this.data.length, samples, equalator, target);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <C extends Consumer<? super E>> C union(
		final XGettingCollection<? extends E> samples,
		final Equalator<? super E> equalator,
		final C target
	)
	{
		return AbstractArrayStorage.union((E[])this.data, this.data.length, samples, equalator, target);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <C extends Consumer<? super E>> C copyTo(final C target)
	{
		return AbstractArrayStorage.copyTo((E[])this.data, this.data.length, target);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <C extends Consumer<? super E>> C filterTo(final C target, final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.copyTo((E[])this.data, this.data.length, target, predicate);
	}

	@Override
	public <T> T[] copyTo(final T[] target, final int offset)
	{
		System.arraycopy(this.data, 0, target, offset, this.data.length);
		return target;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] copyTo(final T[] target, final int targetOffset, final long offset, final int length)
	{
		return AbstractArrayStorage.rngCopyTo(
			(E[])this.data                ,
			this.data.length              ,
			Jadoth.checkArrayRange(offset),
			length                        ,
			target                        ,
			targetOffset
		);
	}

	@SuppressWarnings("unchecked")
	public <T> T[] rngCopyTo(final int startIndex, final int length, final T[] target, final int offset)
	{
		return AbstractArrayStorage.rngCopyTo((E[])this.data, this.data.length, startIndex, length,  target, offset);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <C extends Consumer<? super E>> C distinct(final C target)
	{
		return AbstractArrayStorage.distinct((E[])this.data, this.data.length, target);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <C extends Consumer<? super E>> C distinct(final C target, final Equalator<? super E> equalator)
	{
		return AbstractArrayStorage.distinct((E[])this.data, this.data.length, target, equalator);
	}


	@SuppressWarnings("unchecked")
	@Override
	public <C extends Consumer<? super E>> C copySelection(final C target, final long... indices)
	{
		return AbstractArrayStorage.copySelection((E[])this.data, this.data.length, indices, target);
	}



	///////////////////////////////////////////////////////////////////////////
	// java.util.list and derivatives  //
	////////////////////////////////////

	@Override
	public boolean isEmpty()
	{
		return this.data.length == 0;
	}

	@Override
	public Iterator<E> iterator()
	{
		return new ReadOnlyListIterator<>(this);
	}

	@Override
	public long size()
	{
		return this.data.length;
	}

	@Override
	public long maximumCapacity()
	{
		return this.data.length;
	}

	@Override
	public boolean isFull()
	{
		return true;
	}

	@Override
	public long remainingCapacity()
	{
		return 0;
	}

	@Override
	public ConstLinearEnum<E> view()
	{
		return this;
	}

	@Override
	public ConstLinearEnum<E> view(final long lowIndex, final long highIndex)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

	@Override
	public ConstLinearEnum<E> range(final long fromIndex, final long toIndex)
	{
		// range check is done in constructor
		// (14.06.2011 TM)FIXME: SubConstEnum
		throw new net.jadoth.meta.NotImplementedYetError();
//		return new SubListView<>(this, fromIndex, toIndex);
	}

	@Override
	public String toString()
	{
		return AbstractArrayStorage.toString(this.data, this.data.length);
	}

	@Override
	public Object[] toArray()
	{
		return this.data.clone();
	}

	@SuppressWarnings("unchecked")
	@Override
	public E at(final long index) throws ArrayIndexOutOfBoundsException
	{
		if(index >= this.data.length)
		{
			throw new IndexBoundsException(this.data.length, index);
		}
		return (E)this.data[(int)index];
	}



	@Deprecated
	@Override
	public boolean equals(final Object o)
	{
		//trivial escape conditions
		if(o == this)
		{
			return true;
		}
		if(o == null || !(o instanceof List<?>))
		{
			return false;
		}

		final List<?> list = (List<?>)o;
		if(this.data.length != list.size())
		{
			return false; //lists can only be equal if they have the same length
		}

		final Object[] data = this.data;
		int i = 0;
		for(final Object e2 : list)
		{
			//use iterator for passed list as it could be a non-random-access list
			final Object e1 = data[i++];
			if(e1 == null)
			{
				//null-handling escape conditions
				if(e2 != null)
				{
					return false;
				}
				continue;
			}
			if(!e1.equals(e2))
			{
				return false;
			}
		}
		return true; //no un-equal element found, so lists must be equal
	}

	@Deprecated
	@Override
	public int hashCode()
	{
		return JadothArrays.arrayHashCode(this.data, this.data.length);
	}



	@Override
	public OldConstEnum<E> old()
	{
		return new OldConstEnum<>(this);
	}

	public static final class OldConstEnum<E> extends AbstractOldGettingSet<E>
	{
		OldConstEnum(final ConstLinearEnum<E> list)
		{
			super(list);
		}

		@Override
		public ConstLinearEnum<E> parent()
		{
			return (ConstLinearEnum<E>)super.parent();
		}

	}

}
