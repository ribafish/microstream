package net.jadoth.storage.types;

import static net.jadoth.X.notNull;

import java.io.File;

import net.jadoth.files.XFiles;
import net.jadoth.persistence.internal.PersistenceTypeDictionaryFileHandler;
import net.jadoth.persistence.types.Persistence;
import net.jadoth.persistence.types.PersistenceIdStrategy;
import net.jadoth.persistence.types.PersistenceObjectIdProvider;
import net.jadoth.persistence.types.PersistenceTypeDictionaryIoHandler;
import net.jadoth.persistence.types.PersistenceTypeEvaluator;
import net.jadoth.persistence.types.PersistenceTypeIdProvider;

public final class EmbeddedStorage
{
	/**
	 * Creates an instance of an {@link EmbeddedStorageFoundation} implementation without any assembly parts set.
	 * 
	 * @return
	 */
	public static final EmbeddedStorageFoundation<?> createFoundation()
	{
		return new EmbeddedStorageFoundation.Implementation<>();
	}
	
	public static final EmbeddedStorageConnectionFoundation<?> ConnectionFoundation(
		final PersistenceTypeDictionaryIoHandler typeDictionaryIoHandler,
		final PersistenceIdStrategy                  idStrategy
	)
	{
		return ConnectionFoundation(
			typeDictionaryIoHandler      ,
			idStrategy                   ,
			Persistence::isPersistable   ,
			Persistence::isTypeIdMappable
		);
	}
	
	public static final EmbeddedStorageConnectionFoundation<?> ConnectionFoundation(
		final PersistenceTypeDictionaryIoHandler typeDictionaryIoHandler    ,
		final PersistenceIdStrategy                  idStrategy                 ,
		final PersistenceTypeEvaluator           typeEvaluatorPersistable   ,
		final PersistenceTypeEvaluator           typeEvaluatorTypeIdMappable
	)
	{
		final PersistenceObjectIdProvider objectIdProvider = idStrategy.objectIdStragegy().createObjectIdProvider();
		final PersistenceTypeIdProvider   typeIdProvider   = idStrategy.typeIdStragegy().createTypeIdProvider();
		
		return EmbeddedStorageConnectionFoundation.New()
			.setTypeDictionaryIoHandler    (typeDictionaryIoHandler    )
			.setObjectIdProvider           (objectIdProvider           )
			.setTypeIdProvider             (typeIdProvider             )
			.setTypeEvaluatorPersistable   (typeEvaluatorPersistable   )
			.setTypeEvaluatorTypeIdMappable(typeEvaluatorTypeIdMappable)
		;
	}
	
	public static final EmbeddedStorageConnectionFoundation<?> ConnectionFoundation(final File directory)
	{
		return ConnectionFoundation(
			PersistenceTypeDictionaryFileHandler.NewInDirecoty(directory),
			PersistenceIdStrategy.NewInDirectory(directory)
		);
	}

	
	
	public static final EmbeddedStorageFoundation<?> Foundation(
		final StorageConfiguration                   configuration       ,
		final EmbeddedStorageConnectionFoundation<?> connectionFoundation
	)
	{
		/* (24.09.2018 TM)NOTE:
		 * Configuration and ConnectionFoundation both depend on a File (directory)
		 * So this is the most elementary creator method possible.
		 */
		return createFoundation()
			.setConfiguration(configuration)
			.setConnectionFoundation(connectionFoundation)
		;
	}
	
	public static final EmbeddedStorageFoundation<?> Foundation(
		final StorageFileProvider                    fileProvider        ,
		final EmbeddedStorageConnectionFoundation<?> connectionFoundation
	)
	{
		return createFoundation()
			.setConfiguration(
				Storage.Configuration(fileProvider)
			)
			.setConnectionFoundation(connectionFoundation)
		;
	}
	
	public static final EmbeddedStorageFoundation<?> Foundation(final File directory)
	{
		XFiles.ensureDirectory(notNull(directory));

		return Foundation(
			Storage.FileProvider(directory),
			ConnectionFoundation(directory)
		);
	}

	public static final EmbeddedStorageFoundation<?> Foundation()
	{
		return Foundation(new File(Storage.defaultDirectoryName()));
	}
	
	public static final EmbeddedStorageFoundation<?> Foundation(
		final File                          directory             ,
		final StorageChannelCountProvider   channelCountProvider  ,
		final StorageHousekeepingController housekeepingController,
		final StorageDataFileEvaluator      fileDissolver         ,
		final StorageEntityCacheEvaluator   entityCacheEvaluator
	)
	{
		XFiles.ensureDirectory(directory);

		return Foundation(
			Storage.Configuration(
				Storage.FileProvider(directory),
				channelCountProvider           ,
				housekeepingController         ,
				fileDissolver                  ,
				entityCacheEvaluator
			),
			ConnectionFoundation(directory)
		);
	}
		
	public static final EmbeddedStorageManager start(
		final StorageConfiguration                   configuration       ,
		final EmbeddedStorageConnectionFoundation<?> connectionFoundation
	)
	{
		return start(null, configuration, connectionFoundation);
	}

	public static final EmbeddedStorageManager start(
		final Object                                 explicitRoot        ,
		final StorageConfiguration                   configuration       ,
		final EmbeddedStorageConnectionFoundation<?> connectionFoundation
	)
	{
		final EmbeddedStorageManager esm = Foundation(configuration, connectionFoundation)
			.createEmbeddedStorageManager(explicitRoot)
		;
		esm.start();
		
		return esm;
	}
	
	public static final EmbeddedStorageManager start(
		final StorageFileProvider                    fileProvider        ,
		final EmbeddedStorageConnectionFoundation<?> connectionFoundation
	)
	{
		return start(null, fileProvider, connectionFoundation);
	}
	
	public static final EmbeddedStorageManager start(
		final Object                                 explicitRoot        ,
		final StorageFileProvider                    fileProvider        ,
		final EmbeddedStorageConnectionFoundation<?> connectionFoundation
	)
	{
		final EmbeddedStorageManager esm = Foundation(fileProvider, connectionFoundation)
			.createEmbeddedStorageManager()
		;
		esm.start();
		
		return esm;
	}
	
	public static final EmbeddedStorageManager start(final File directory)
	{
		return start(null, directory);
	}
	
	public static final EmbeddedStorageManager start(final Object explicitRoot, final File directory)
	{
		final EmbeddedStorageManager esm = Foundation(directory)
			.createEmbeddedStorageManager(explicitRoot)
		;
		esm.start();
		
		return esm;
	}

	/**
	 * Uber-simplicity util method. See {@link #ensureStorageManager()} and {@link #Foundation()} variants for
	 * more practical alternatives.
	 * 
	 * @return An {@link EmbeddedStorageManager} instance with an actively running database using all-default-settings.
	 */
	public static final EmbeddedStorageManager start()
	{
		return start((Object)null); // no explicit root. Not to be confused with start(File)
	}
	
	public static final EmbeddedStorageManager start(final Object explicitRoot)
	{
		final EmbeddedStorageManager esm = Foundation()
			.createEmbeddedStorageManager(explicitRoot)
		;
		esm.start();
		
		return esm;
	}
	
	public static final EmbeddedStorageManager start(
		final Object                        explicitRoot          ,
		final File                          directory             ,
		final StorageChannelCountProvider   channelCountProvider  ,
		final StorageHousekeepingController housekeepingController,
		final StorageDataFileEvaluator      fileDissolver         ,
		final StorageEntityCacheEvaluator   entityCacheEvaluator
	)
	{
		final EmbeddedStorageManager esm = Foundation(
			directory             ,
			channelCountProvider  ,
			housekeepingController,
			fileDissolver         ,
			entityCacheEvaluator
		)
		.createEmbeddedStorageManager(explicitRoot).start();
		
		return esm;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	private EmbeddedStorage()
	{
		// static only
		throw new UnsupportedOperationException();
	}

}
