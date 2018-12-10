package net.jadoth.persistence.binary.internal;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.persistence.types.PersistenceLoadHandler;
import net.jadoth.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerNativeArray_char extends AbstractBinaryHandlerNativeArrayPrimitive<char[]>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerNativeArray_char()
	{
		super(char[].class, defineElementsType(char.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(final Binary bytes, final char[] array, final long oid, final PersistenceStoreHandler handler)
	{
		BinaryPersistence.storeArray_char(bytes, this.typeId(), oid, array);
	}

	@Override
	public char[] create(final Binary bytes)
	{
		return BinaryPersistence.createArray_char(bytes);
	}

	@Override
	public void update(final Binary bytes, final char[] instance, final PersistenceLoadHandler builder)
	{
		BinaryPersistence.updateArray_char(instance, bytes);
	}

}
