package one.microstream.storage.types;

import java.util.function.Consumer;
import java.util.function.Supplier;

import one.microstream.exceptions.MissingFoundationPartException;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceFoundation;
import one.microstream.persistence.types.PersistenceObjectIdProvider;
import one.microstream.persistence.types.PersistenceRefactoringMappingProvider;
import one.microstream.persistence.types.PersistenceRootResolver;
import one.microstream.persistence.types.PersistenceRootResolverProvider;
import one.microstream.persistence.types.PersistenceRootsProvider;
import one.microstream.persistence.types.PersistenceTypeDictionary;
import one.microstream.persistence.types.PersistenceTypeHandlerManager;
import one.microstream.persistence.types.PersistenceTypeHandlerRegistration;
import one.microstream.persistence.types.PersistenceTypeManager;


/**
 * A kind of factory type that holds and creates on demand all the parts that form an {@link EmbeddedStorageManager}
 * instance, i.e. a functional database handling logic embedded in the same process as the application using it.
 * <p>
 * Additionally to the services of a mere factory type, a foundation type also keeps references to all parts
 * after a {@link EmbeddedStorageManager} instance has been created. This is useful if some internal logic parts
 * shall be accessed while the {@link EmbeddedStorageManager} logic is already running. Therefore, this type can
 * best be thought of as a {@literal foundation} on which the running database handling logic stands.
 * <p>
 * All {@literal set~} methods are simple setter methods without any additional logic worth mentioning.<br>
 * All {@literal set~} methods return {@literal this} to allow for easy method chaining to improve readability.<br>
 * All {@literal get~} methods return a logic part instance, if present or otherwise creates and sets one beforehand
 * via a default creation logic.
 * 
 * @author TM
 *
 * @param <F> the "self-type" of the  {@link EmbeddedStorageManager} implementation.
 */
public interface EmbeddedStorageFoundation<F extends EmbeddedStorageFoundation<?>> extends StorageFoundation<F>
{
	/**
	 * Returns the currently set {@link StorageConfiguration} instance.
	 * <p>
	 * If no instance is set and the implementation deems an instance of this type mandatory for the successful
	 * executon of {@link #createEmbeddedStorageManager()}, a suitable instance is created via an internal default
	 * creation logic and then set as the current. If the implementation has not sufficient logic and/or data
	 * to create a default instance, a {@link MissingFoundationPartException} is thrown.
	 * 
	 * @return the currently set instance, potentially created on-demand if required.
	 * 
	 * @throws MissingFoundationPartException if a returnable instance is required but cannot be created by default.
	 */
	public EmbeddedStorageConnectionFoundation<?> getConnectionFoundation();
	
	/**
	 * Returns the internal {@link EmbeddedStorageConnectionFoundation} instance's
	 * {@link PersistenceRootResolverProvider} instance. If none is present so far, a new default one is created.
	 * 
	 * @return the {@link PersistenceRootResolverProvider} instance to be used.
	 */
	public PersistenceRootResolverProvider getRootResolverProvider();
	
	/**
	 * Executes the passed {@literal logic} on the {@link EmbeddedStorageConnectionFoundation} instance provided
	 * by {@link #getConnectionFoundation()}.
	 * <p>
	 * This is a mere utility method to allow more concise syntax and multi-layered method chaining.
	 * 
	 * @param logic the {@literal logic} to be executed.
	 * 
	 * @return {@literal this} to allow method chaining.
	 */
	public F onConnectionFoundation(Consumer<? super EmbeddedStorageConnectionFoundation<?>> logic);
	
	/**
	 * Executes the passed {@literal logic} on {@literal this}.
	 * <p>
	 * This is a mere utility method to allow more concise syntax and multi-layered method chaining.
	 * 
	 * @param logic the {@literal logic} to be executed.
	 * 
	 * @return {@literal this} to allow method chaining.
	 */
	public F onThis(Consumer<? super EmbeddedStorageFoundation<?>> logic);

	/**
	 * Creates and returns a new {@link EmbeddedStorageManager} instance by using the current state of all registered
	 * logic part instances and by on-demand creating missing ones via a default logic.
	 * <p>
	 * Alias for {@code return this.createEmbeddedStorageManager(null);}
	 * <p>
	 * The returned {@link EmbeddedStorageManager} instance will NOT yet be started.
	 * 
	 * @return {@linkDoc EmbeddedStorageFoundation#createEmbeddedStorageManager(Object)@return}
	 * 
	 * @see #createEmbeddedStorageManager(Object)
	 * @see #start()
	 * @see #start(Object)
	 */
	public default EmbeddedStorageManager createEmbeddedStorageManager()
	{
		// no explicit root by default
		return this.createEmbeddedStorageManager(null);
	}
	
	/**
	 * Creates and returns a new {@link EmbeddedStorageManager} instance by using the current state of all registered
	 * logic part instances and by on-demand creating missing ones via a default logic.
	 * <p>
	 * If the passed {@literal explicitRoot} is {@literal null}, a default root instance will be created, see
	 * {@link EmbeddedStorageManager#defaultRoot()}.
	 * <p>
	 * The returned {@link EmbeddedStorageManager} instance will NOT yet be started.
	 * 
	 * @param explicitRoot the instance to be used as the persistent entity graph's root instance.
	 * 
	 * @return a new {@link EmbeddedStorageManager} instance.
	 * 
	 * @see #createEmbeddedStorageManager()
	 * @see #createEmbeddedStorageManager(Supplier)
	 * @see #start()
	 * @see #start(Object)
	 */
	public EmbeddedStorageManager createEmbeddedStorageManager(Object explicitRoot);
		
	/**
	 * Convenience method to create, start and return an {@link EmbeddedStorageManager} instance using a default
	 * root instance.
	 * 
	 * @return {@linkDoc EmbeddedStorageFoundation#createEmbeddedStorageManager(Object)@return}
	 * 
	 * @see #start(Object)
	 * @see #createEmbeddedStorageManager()
	 * @see #createEmbeddedStorageManager(Object)
	 */
	public default EmbeddedStorageManager start()
	{
		return this.start(null);
	}
	
	/*
	 * Funny how they can't create a properly functioning way to write multi-lined code in a JavaDoc.
	 * See https://reflectoring.io/howto-format-code-snippets-in-javadoc/ for a well-written overview
	 * over <pre>, <code> and {@code}.
	 * In addition to that, the <pre>{@code} combination causes weird things with spaces at the beginning
	 * of lines. At least in the eclipse JavaDoc view, but that is almost as important as the HTML views.
	 * In short: all variants are inadequat. The best solution is to write every line in its own @code tag.
	 * A real shame. As usual in the JDK.
	 */
	/**
	 * Convenience method to create, start and return an {@link EmbeddedStorageManager} instance using the
	 * passed {@literal explicitRoot}
	 * <p>
	 * By default, it is an alias for:<br>
	 * {@code EmbeddedStorageManager esm = this.createEmbeddedStorageManager(explicitRoot);}<br>
	 * {@code esm.start();}<br>
	 * {@code return esm;}
	 * 
	 * @param explicitRoot {@linkDoc EmbeddedStorageFoundation#createEmbeddedStorageManager(Object):}
	 * 
	 * @return {@linkDoc EmbeddedStorageFoundation#createEmbeddedStorageManager(Object)@return}
	 * 
	 * @see #start()
	 * @see #createEmbeddedStorageManager()
	 * @see #createEmbeddedStorageManager(Object)
	 */
	public default EmbeddedStorageManager start(final Object explicitRoot)
	{
		final EmbeddedStorageManager esm = this.createEmbeddedStorageManager(explicitRoot);
		esm.start();
		return esm;
	}

	/**
	 * Sets the {@link EmbeddedStorageConnectionFoundation} instance to be used for the assembly.
	 * 
	 * @param connectionFoundation the instance to be used.
	 * 
	 * @return {@literal this} to allow method chaining.
	 */
	public F setConnectionFoundation(EmbeddedStorageConnectionFoundation<?> connectionFoundation);
	
	/**
	 * Registers the passed {@literal root} instance as the root instance at the
	 * {@link EmbeddedStorageConnectionFoundation} instance provided by
	 * {@link #getConnectionFoundation()}.<br>
	 * Use {@link #setRootSupplier(Supplier)} for a more dynamic approach, i.e. if the actual root
	 * instance must be created after setting up and creating the {@link EmbeddedStorageManager}.
	 * 
	 * @param root the instance to be used as the persistent entity graph's root instance.
	 * 
	 * @return {@literal this} to allow method chaining.
	 * 
	 * @see #setRootSupplier(Supplier)
	 * @see #setRootResolverProvider(PersistenceRootResolver)
	 * @see EmbeddedStorageConnectionFoundation#setRootResolverProvider(PersistenceRootResolver)
	 */
	public F setRoot(Object root);

	/**
	 * Registers the passed {@literal rootSupplier} {@link Supplier} as the root instance supplier at the
	 * {@link PersistenceRootResolverProvider} instance provided by
	 * {@link EmbeddedStorageConnectionFoundation#getRootResolverProvider()} of the
	 * {@link EmbeddedStorageConnectionFoundation} instance provided by
	 * {@link #getConnectionFoundation()}.
	 * <p>
	 * This means this method is merely an alias for
	 * <pre>{@code .onConnectionFoundation(f ->
	 *{
	 *	f.getRootResolverProvider().registerCustomRootSupplier(rootSupplier);
	 *})
	 *
	 *}</pre>
	 * <p>
	 * Note that replacing the {@link PersistenceRootResolverProvider} instance, for example
	 * via #setRootResolverProvider, will nullify the changes made via this method.
	 * <p>
	 * The actual root instance will be queried during startup, not before.<br>
	 * This technique allows a more dynamic approach than {@link #setRoot(Object)}, i.e. if the actual root
	 * instance must be created after setting up and creating the {@link EmbeddedStorageManager}.
	 * 
	 * @param rootSupplier the supplying logic to obtain the instance to be used during startup
	 *        as the persistent entity graph's root instance.
	 * 
	 * @return {@literal this} to allow method chaining.
	 * 
	 * @see EmbeddedStorageManager#start()
	 * @see #setRoot(Object)
	 * @see #setRootResolverProvider(PersistenceRootResolver)
	 * @see EmbeddedStorageConnectionFoundation#setRootResolverProvider(PersistenceRootResolver)
	 * @see EmbeddedStorageConnectionFoundation#registerCustomRootSupplier(PersistenceRootResolver)
	 */
	public F setRootSupplier(Supplier<?> rootSupplier);

	/**
	 * Alias for <pre>{@code .onConnectionFoundation(f ->
	 *{
	 *	f.setRootResolverProvider(rootResolverProvider);
	 *})
	 *
	 *}</pre>
	 *
	 *Sets the {@link PersistenceRootResolverProvider} instance to be used in the internal
	 *{@link EmbeddedStorageConnectionFoundation}.
	 * 
	 * @param rootResolverProvider the {@link PersistenceRootResolverProvider} to be set.
	 * 
	 * @return {@literal this} to allow method chaining.
	 * 
	 * @see #setRootSupplier(Supplier)
	 * @see EmbeddedStorageConnectionFoundation#setRootResolverProvider(PersistenceRootResolver)
	 * 
	 */
	public F setRootResolverProvider(PersistenceRootResolverProvider rootResolverProvider);
	
	/**
	 * Sets the passed {@link PersistenceRefactoringMappingProvider} instance to the
	 * {@link EmbeddedStorageConnectionFoundation} instance provided by {@link #getConnectionFoundation()}.
	 * 
	 * @param refactoringMappingProvider the instance to be used.
	 * 
	 * @return {@literal this} to allow method chaining.
	 * 
	 * @see EmbeddedStorageConnectionFoundation#setRefactoringMappingProvider(PersistenceRefactoringMappingProvider)
	 */
	public F setRefactoringMappingProvider(PersistenceRefactoringMappingProvider refactoringMappingProvider);

	/**
	 * Convenience method for {@code this.getConnectionFoundation().executeTypeHandlerRegistration(typeHandlerRegistration)}.
	 * <p>
	 * See {@link PersistenceFoundation#executeTypeHandlerRegistration(PersistenceTypeHandlerRegistration)} for details.
	 * 
	 * @param typeHandlerRegistration the {@link PersistenceTypeHandlerRegistration} to be executed.
	 * 
	 * @return {@literal this} to allow method chaining.
	 */
	public F executeTypeHandlerRegistration(PersistenceTypeHandlerRegistration<Binary> typeHandlerRegistration);
	
	
	
	/**
	 * Pseudo-constructor method to create a new {@link EmbeddedStorageFoundation} instance with default implementation.
	 * 
	 * @return a new {@link EmbeddedStorageFoundation} instance.
	 */
	public static EmbeddedStorageFoundation<?> New()
	{
		return new EmbeddedStorageFoundation.Default<>();
	}

	public class Default<F extends EmbeddedStorageFoundation.Default<?>>
	extends StorageFoundation.Default<F>
	implements EmbeddedStorageFoundation<F>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private EmbeddedStorageConnectionFoundation<?> connectionFoundation;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default()
		{
			super();
		}

		

		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public F onConnectionFoundation(
			final Consumer<? super EmbeddedStorageConnectionFoundation<?>> logic
		)
		{
			final EmbeddedStorageConnectionFoundation<?> escf = this.getConnectionFoundation();
			logic.accept(escf);
			
			return this.$();
		}
		

		@Override
		public F onThis(final Consumer<? super EmbeddedStorageFoundation<?>> logic)
		{
			logic.accept(this);
			
			return this.$();
		}
		
		@Override
		public F setRoot(final Object root)
		{
			this.getConnectionFoundation().getRootResolverProvider().registerRoot(root);
			
			return this.$();
		}
		
		@Override
		public F setRootSupplier(final Supplier<?> rootSupplier)
		{
			this.getConnectionFoundation().getRootResolverProvider().registerRootSupplier(rootSupplier);
			
			return this.$();
		}

		@Override
		public F setRootResolverProvider(final PersistenceRootResolverProvider rootResolverProvider)
		{
			this.getConnectionFoundation().setRootResolverProvider(rootResolverProvider);
			
			return this.$();
		}
		
		protected EmbeddedStorageConnectionFoundation<?> createConnectionFoundation()
		{
			throw new MissingFoundationPartException(EmbeddedStorageConnectionFoundation.class);
//			return new EmbeddedStorageConnectionFoundation.Default();
		}

		@Override
		protected EmbeddedStorageRootTypeIdProvider ensureRootTypeIdProvider()
		{
			final EmbeddedStorageConnectionFoundation<?> escf          = this.getConnectionFoundation();
			final PersistenceRootsProvider<Binary>       rootsProvider = escf.getRootsProvider();
			
			// the genericness of this :D (albeit #provideRoots is implicitly assumed to cache the instance)
			return EmbeddedStorageRootTypeIdProvider.New(
				rootsProvider.provideRoots().getClass()
			);
		}

		@Override
		public EmbeddedStorageConnectionFoundation<?> getConnectionFoundation()
		{
			if(this.connectionFoundation == null)
			{
				this.connectionFoundation = this.dispatch(this.createConnectionFoundation());
			}
			
			return this.connectionFoundation;
		}
		
		@Override
		public PersistenceRootResolverProvider getRootResolverProvider()
		{
			return this.getConnectionFoundation().getRootResolverProvider();
		}

		/* (02.03.2014 TM)TODO: Storage Configuration more dynamic
		 *  The configuration must be provided in the creation process, not set idependantly.
		 *  Example: cache evaluator might have to know all the channel caches.
		 *  To avoid initializer loops (configuration must exist for the channels to be created),
		 *  an initialize(...) pattern must be used.
		 *
		 *  Speaking of which:
		 *  It may generally be a good idea to retrofit numerous types with initialize(...) methods
		 *  (mostly for backreferencing channels). If an implementation doesn't need the backreference,
		 *  the initialize method(s) can simply be no-op.
		 *  An initialization tree diagram should be created to asses the initialization dependancies.
		 */
		@Override
		public F setConfiguration(final StorageConfiguration configuration)
		{
			super.setConfiguration(configuration);
			return this.$();
		}

		@Override
		public F setRequestAcceptorCreator(
			final StorageRequestAcceptor.Creator requestAcceptorCreator
		)
		{
			super.setRequestAcceptorCreator(requestAcceptorCreator);
			return this.$();
		}

		@Override
		public F setTaskBrokerCreator(
			final StorageTaskBroker.Creator taskBrokerCreator
		)
		{
			super.setTaskBrokerCreator(taskBrokerCreator);
			return this.$();
		}

		@Override
		public F setDataChunkValidatorProvider(
			final StorageDataChunkValidator.Provider dataChunkValidatorProvider
		)
		{
			super.setDataChunkValidatorProvider(dataChunkValidatorProvider);
			return this.$();
		}

		@Override
		public F setChannelCreator(final StorageChannelsCreator channelCreator)
		{
			super.setChannelCreator(channelCreator);
			return this.$();
		}

		@Override
		public F setTaskCreator(final StorageRequestTaskCreator taskCreator)
		{
			super.setTaskCreator(taskCreator);
			return this.$();
		}

		@Override
		public F setTypeDictionary(final StorageTypeDictionary typeDictionary)
		{
			super.setTypeDictionary(typeDictionary);
			return this.$();
		}

		@Override
		public F setRootTypeIdProvider(
			final StorageRootTypeIdProvider rootTypeIdProvider
		)
		{
			super.setRootTypeIdProvider(rootTypeIdProvider);
			return this.$();
		}

		@Override
		public F setConnectionFoundation(
			final EmbeddedStorageConnectionFoundation<?> connectionFoundation
		)
		{
			this.connectionFoundation = connectionFoundation;

			/* Tricky: this instance must be set as a callback StorageManager supplier in case
			 * the getStorageManager method is called before createEmbeddedStorageManager.
			 * E.g.: setting customizing logic
			 */
			this.connectionFoundation.setStorageManagerSupplier(() ->
				this.createStorageSystem()
			);
			
			return this.$();
		}
		
		@Override
		public F setRefactoringMappingProvider(
			final PersistenceRefactoringMappingProvider refactoringMappingProvider
		)
		{
			this.getConnectionFoundation().setRefactoringMappingProvider(refactoringMappingProvider);
			return this.$();
		}

		@Override
		public F setTimestampProvider(
			final StorageTimestampProvider timestampProvider
		)
		{
			super.setTimestampProvider(timestampProvider);
			return this.$();
		}

		private void initializeEmbeddedStorageRootTypeIdProvider(
			final StorageRootTypeIdProvider rootTypeIdProvider,
			final PersistenceTypeManager        typeIdLookup
		)
		{
			// a little hacky instanceof here, maybe refactor to cleaner structure in the future
			if(rootTypeIdProvider instanceof EmbeddedStorageRootTypeIdProvider)
			{
				((EmbeddedStorageRootTypeIdProvider)rootTypeIdProvider).initialize(typeIdLookup);
			}
			// on the other hand, this is a very flexible way of still allowing another rootTypeId source
		}

		@Override
		protected StorageObjectIdRangeEvaluator ensureObjectIdRangeEvaluator()
		{
			final PersistenceObjectIdProvider oip = this.getConnectionFoundation().getObjectIdProvider();
			return (min, max) ->
			{
				// update OID provider if necessary to avoid OID inconsistencies.
				if(max > oip.currentObjectId())
				{
					oip.updateCurrentObjectId(max);
				}
			};
		}
		
		@Override
		public F executeTypeHandlerRegistration(final PersistenceTypeHandlerRegistration<Binary> typeHandlerRegistration)
		{
			this.getConnectionFoundation().executeTypeHandlerRegistration(typeHandlerRegistration);
			return this.$();
		}
						
		@Override
		public synchronized EmbeddedStorageManager createEmbeddedStorageManager(final Object root)
		{
			final EmbeddedStorageConnectionFoundation<?> ecf = this.getConnectionFoundation();
			
			// explicit root must be registered at the rootResolverProvider.
			if(root != null)
			{
				ecf.getRootResolverProvider().registerRoot(root);
			}
			
			// must be created BEFORE the type handler manager is initilized to register its custom type handler
			final PersistenceRootsProvider<Binary> prp = ecf.getRootsProvider();

			// initialize persistence (=binary) type handler manager (validate and ensure type handlers)
			final PersistenceTypeHandlerManager<?> thm = ecf.getTypeHandlerManager();
			thm.initialize();
			
			// the registered supplier callback leads back to this class' createStorageManager method
			final StorageSystem stm = ecf.getStorageManager();
			
			initializeTypeDictionary(stm, ecf);

			// resolve root types to root type ids after types have been initialized
			this.initializeEmbeddedStorageRootTypeIdProvider(this.getRootTypeIdProvider(), thm);
				
			// everything bundled together in the actual manager instance
			return EmbeddedStorageManager.New(stm.configuration(), ecf, prp);
		}
		
		private static void initializeTypeDictionary(
			final StorageSystem                         stm,
			final EmbeddedStorageConnectionFoundation<?> ecf
		)
		{
			final StorageTypeDictionary     std = stm.typeDictionary();
			final PersistenceTypeDictionary ptd = ecf.getTypeDictionaryProvider().provideTypeDictionary();
			
			// The two level's dictionary instances are a little intertwined (as they must be)
			std.initialize(ptd);
			ptd.setTypeDescriptionRegistrationObserver(std);
		}

	}

}
