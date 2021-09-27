/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * AsyncWorldEdit Injector a hack plugin that allows AsyncWorldEdit to integrate with
 * the WorldEdit plugin.
 *
 * Copyright (c) 2014, SBPrime <https://github.com/SBPrime/>
 * Copyright (c) AsyncWorldEdit contributors
 * Copyright (c) AsyncWorldEdit injector contributors
 *
 * All rights reserved.
 *
 * Redistribution in source, use in source and binary forms, with or without
 * modification, are permitted free of charge provided that the following
 * conditions are met:
 *
 * 1.  Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 * 2.  Redistributions of source code, with or without modification, in any form
 *     other then free of charge is not allowed,
 * 3.  Redistributions of source code, with tools and/or scripts used to build the 
 *     software is not allowed,
 * 4.  Redistributions of source code, with information on how to compile the software
 *     is not allowed,
 * 5.  Providing information of any sort (excluding information from the software page)
 *     on how to compile the software is not allowed,
 * 6.  You are allowed to build the software for your personal use,
 * 7.  You are allowed to build the software using a non public build server,
 * 8.  Redistributions in binary form in not allowed.
 * 9.  The original author is allowed to redistrubute the software in bnary form.
 * 10. Any derived work based on or containing parts of this software must reproduce
 *     the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the
 *     derived work.
 * 11. The original author of the software is allowed to change the license
 *     terms or the entire license of the software as he sees fit.
 * 12. The original author of the software is allowed to sublicense the software
 *     or its parts using any license terms he sees fit.
 * 13. By contributing to this project you agree that your contribution falls under this
 *     license.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.primesoft.asyncworldedit.injector.core;

import org.primesoft.asyncworldedit.injector.core.visitors.LocalSessionVisitor;
import org.primesoft.asyncworldedit.injector.core.visitors.WrapGetWorldVisitor;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.primesoft.asyncworldedit.injector.IClassInjector;
import org.primesoft.asyncworldedit.injector.classfactory.IClassFactory;
import org.primesoft.asyncworldedit.injector.classfactory.base.BaseClassFactory;
import org.primesoft.asyncworldedit.injector.core.visitors.AsyncWrapperVisitor;
import org.primesoft.asyncworldedit.injector.core.visitors.BaseClassCreator;
import org.primesoft.asyncworldedit.injector.core.visitors.extent.clipboard.BlockArrayClipboardClassVisitor;
import org.primesoft.asyncworldedit.injector.core.visitors.CreateActorFactory;
import org.primesoft.asyncworldedit.injector.core.visitors.CreateNoPermsActor;
import org.primesoft.asyncworldedit.injector.core.visitors.CreateNoPermsPlayer;
import org.primesoft.asyncworldedit.injector.core.visitors.CreatePlayerFactory;
import org.primesoft.asyncworldedit.injector.core.visitors.CreatePlayerWrapper;
import org.primesoft.asyncworldedit.injector.core.visitors.ICreateClass;
import org.primesoft.asyncworldedit.injector.core.visitors.InjectorClassVisitor;
import org.primesoft.asyncworldedit.injector.core.visitors.session.SessionManagerVisitor;
import org.primesoft.asyncworldedit.injector.core.visitors.worldedit.BaseRegionExtentSetter;
import org.primesoft.asyncworldedit.injector.core.visitors.worldedit.EditSessionBuilderVisitor;
import org.primesoft.asyncworldedit.injector.core.visitors.worldedit.WorldEditVisitor;
import org.primesoft.asyncworldedit.injector.core.visitors.worldedit.command.tool.AreaPickaxeVisitor;
import org.primesoft.asyncworldedit.injector.core.visitors.worldedit.command.tool.FloodFillToolVisitor;
import org.primesoft.asyncworldedit.injector.core.visitors.worldedit.command.tool.RecursivePickaxeVisitor;
import org.primesoft.asyncworldedit.injector.core.visitors.worldedit.command.tool.TreePlanterVisitor;
import org.primesoft.asyncworldedit.injector.core.visitors.worldedit.extent.AbstractDelegateExtentVisitor;
import org.primesoft.asyncworldedit.injector.core.visitors.worldedit.extent.AbstractExtentMaskVisitor;
import org.primesoft.asyncworldedit.injector.core.visitors.worldedit.extent.ChangeSetExtentVisitor;
import org.primesoft.asyncworldedit.injector.core.visitors.worldedit.extent.world.ChunkLoadingExtentVisitor;
import org.primesoft.asyncworldedit.injector.core.visitors.worldedit.function.operation.OperationsClassVisitor;
import org.primesoft.asyncworldedit.injector.core.visitors.worldedit.EditSessionClassVisitor;
import org.primesoft.asyncworldedit.injector.core.visitors.worldedit.bukkit.BukkitEntityVisitor;
import org.primesoft.asyncworldedit.injector.core.visitors.worldedit.command.CommandsRegistrationVisitor;
import org.primesoft.asyncworldedit.injector.core.visitors.worldedit.command.RegionCommandsVisitor;
import org.primesoft.asyncworldedit.injector.core.visitors.worldedit.command.SchematicCommandsVisitor;
import org.primesoft.asyncworldedit.injector.core.visitors.worldedit.command.ScriptingCommandsVisitor;
import org.primesoft.asyncworldedit.injector.core.visitors.worldedit.command.SnapshotUtilCommandsVisitor;
import org.primesoft.asyncworldedit.injector.core.visitors.worldedit.command.tool.BlockReplacerVisitor;
import org.primesoft.asyncworldedit.injector.core.visitors.worldedit.command.tool.BrushToolVisitor;
import org.primesoft.asyncworldedit.injector.core.visitors.worldedit.extent.reorder.ResetableExtentVisitor;
import org.primesoft.asyncworldedit.injector.core.visitors.worldedit.regions.RegionVisitor;
import org.primesoft.asyncworldedit.injector.core.visitors.worldedit.util.collection.LocatedBlockListVisitor;
import org.primesoft.asyncworldedit.injector.core.visitors.worldedit.util.eventbus.EventBusVisitor;
import org.primesoft.asyncworldedit.utils.ExceptionHelper;

/**
 *
 * @author SBPrime
 */
public class InjectorCore {

    /**
     * Injector core instance
     */
    private static InjectorCore s_instance = null;

    /**
     * Instance MTA mutex
     */
    private final static Object s_mutex = new Object();

    /**
     * Get the static instance
     *
     */
    public static InjectorCore getInstance() {
        if (s_instance == null) {
            synchronized (s_mutex) {
                if (s_instance == null) {
                    s_instance = new InjectorCore();
                }
            }
        }

        return s_instance;
    }

    /**
     * The platform specific API
     */
    private IInjectorPlatform m_platform;

    /**
     * The WorldEdit class factory
     */
    private IClassFactory m_classFactory = new BaseClassFactory();

    private IClassInjector m_classInjector;

    /**
     * The MTA access mutex
     */
    private final Object m_mutex = new Object();

    private List<String> m_log = new LinkedList<>();

    /**
     * Log a console message
     *
     */
    private void log(String message) {
        IInjectorPlatform platform = m_platform;
        if (platform == null) {
            return;
        }

        m_platform.log(message);
    }

    private void logClean() {
        m_log = Collections.EMPTY_LIST;
    }

    private void logDump() {
        m_log.forEach(this::log);
        logClean();
    }

    private void logQueue(String msg) {
        m_log.add(msg);
    }

    /**
     * Initialize the injector core
     *
     */
    public boolean initialize(IInjectorPlatform platform, IClassInjector classInjector) {
        synchronized (m_mutex) {
            if (m_platform != null) {
                log("Injector platform is already set to "
                        + m_platform.getPlatformName() + "."
                        + "Ignoring new platform " + platform.getPlatformName());
                return false;
            }

            m_platform = platform;
            m_classInjector = classInjector;

            log("Injector platform set to: " + platform.getPlatformName());
        }

        return injectClasses();
    }

    private boolean injectClasses() {
        try {
            log("Injecting NMS classes...");
            m_classInjector.getNmsInjection().consume(new NmsClassInjectorBridge());

            log("Injecting WorldEdit classes...");
            modifyClasses("com.sk89q.worldedit.util.eventbus.EventBus", EventBusVisitor::new);

            modifyClasses("com.sk89q.worldedit.math.BlockVector2", AsyncWrapperVisitor::new);
            modifyClasses("com.sk89q.worldedit.math.BlockVector3", AsyncWrapperVisitor::new);
            modifyClasses("com.sk89q.worldedit.math.Vector2", AsyncWrapperVisitor::new);
            modifyClasses("com.sk89q.worldedit.math.Vector3", AsyncWrapperVisitor::new);

            modifyClasses("com.sk89q.worldedit.world.block.BlockStateHolder", AsyncWrapperVisitor::new);
            modifyClasses("com.sk89q.worldedit.world.block.BaseBlock", AsyncWrapperVisitor::new);
            modifyClasses("com.sk89q.worldedit.world.block.BlockState", AsyncWrapperVisitor::new);
            modifyClasses("com.sk89q.worldedit.entity.BaseEntity", AsyncWrapperVisitor::new);
            modifyClasses("com.sk89q.worldedit.util.Location", AsyncWrapperVisitor::new);
            modifyClasses("com.sk89q.worldedit.world.biome.BiomeType", AsyncWrapperVisitor::new);
            modifyClasses("com.sk89q.worldedit.world.weather.WeatherType", AsyncWrapperVisitor::new);

            modifyClasses("com.sk89q.worldedit.EditSession", EditSessionClassVisitor::new);
            modifyClasses("com.sk89q.worldedit.extent.AbstractDelegateExtent", AbstractDelegateExtentVisitor::new);

            modifyClasses("com.sk89q.worldedit.function.operation.Operations", OperationsClassVisitor::new);
            modifyClasses("com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard", BlockArrayClipboardClassVisitor::new);
            modifyClasses("com.sk89q.worldedit.extension.platform.PlayerProxy", WrapGetWorldVisitor::new);

            modifyClasses("com.sk89q.worldedit.util.collection.LocatedBlockList", LocatedBlockListVisitor::new);
            modifyClasses("com.sk89q.worldedit.extent.reorder.MultiStageReorder", ResetableExtentVisitor::new);
            modifyClasses("com.sk89q.worldedit.extent.reorder.ChunkBatchingExtent", ResetableExtentVisitor::new);

            // Regions
            modifyClasses("com.sk89q.worldedit.regions.AbstractRegion", RegionVisitor::new);
            modifyClasses("com.sk89q.worldedit.regions.EllipsoidRegion", RegionVisitor::new);
            modifyClasses("com.sk89q.worldedit.regions.ConvexPolyhedralRegion", RegionVisitor::new);
            modifyClasses("com.sk89q.worldedit.regions.TransformRegion", RegionVisitor::new);
            modifyClasses("com.sk89q.worldedit.regions.RegionIntersection", RegionVisitor::new);
            modifyClasses("com.sk89q.worldedit.regions.NullRegion", RegionVisitor::new);
            modifyClasses("com.sk89q.worldedit.regions.Polygonal2DRegion", RegionVisitor::new);
            modifyClasses("com.sk89q.worldedit.regions.CylinderRegion", RegionVisitor::new);
            modifyClasses("com.sk89q.worldedit.regions.CuboidRegion", RegionVisitor::new);

            // Commands
            modifyClasses("com.sk89q.worldedit.command.SnapshotUtilCommands", SnapshotUtilCommandsVisitor::new);
            modifyClasses("com.sk89q.worldedit.command.ScriptingCommands", ScriptingCommandsVisitor::new);
            modifyClasses("com.sk89q.worldedit.command.SchematicCommands", SchematicCommandsVisitor::new);
            modifyClasses("com.sk89q.worldedit.command.RegionCommands", RegionCommandsVisitor::new);

            modifyClasses("com.sk89q.worldedit.command.UtilityCommandsRegistration", CommandsRegistrationVisitor::new);
            modifyClasses("com.sk89q.worldedit.bukkit.BukkitEntity", BukkitEntityVisitor::new);
            
            // Bukkit
            modifyClasses("com.sk89q.worldedit.command.RegionCommandsRegistration", CommandsRegistrationVisitor::new);
            
            // Reflection
            modifyClasses("com.sk89q.worldedit.function.mask.AbstractExtentMask", AbstractExtentMaskVisitor::new);
            modifyClasses("com.sk89q.worldedit.LocalSession", LocalSessionVisitor::new);
            modifyClasses("com.sk89q.worldedit.session.SessionManager", SessionManagerVisitor::new);
            modifyClasses("com.sk89q.worldedit.extent.ChangeSetExtent", ChangeSetExtentVisitor::new);
            modifyClasses("com.sk89q.worldedit.extent.world.ChunkLoadingExtent", ChunkLoadingExtentVisitor::new);
            modifyClasses("com.sk89q.worldedit.command.tool.BrushTool", BrushToolVisitor::new);
            modifyClasses("com.sk89q.worldedit.command.tool.BlockReplacer", BlockReplacerVisitor::new);
            modifyClasses("com.sk89q.worldedit.command.tool.FloodFillTool", FloodFillToolVisitor::new);
            modifyClasses("com.sk89q.worldedit.command.tool.TreePlanter", TreePlanterVisitor::new);
            modifyClasses("com.sk89q.worldedit.command.tool.AreaPickaxe", AreaPickaxeVisitor::new);
            modifyClasses("com.sk89q.worldedit.command.tool.RecursivePickaxe", RecursivePickaxeVisitor::new);

            modifyClasses("com.sk89q.worldedit.WorldEdit", WorldEditVisitor::new);
            modifyClasses("com.sk89q.worldedit.EditSessionBuilder", EditSessionBuilderVisitor::new);

            for (String n : new String[]{
                "com.sk89q.worldedit.function.entity.ExtentEntityCopy",
                "com.sk89q.worldedit.function.block.ExtentBlockCopy",
                "com.sk89q.worldedit.function.block.BlockReplace",
                "com.sk89q.worldedit.function.operation.ForwardExtentCopy",
                "com.sk89q.worldedit.function.operation.SetLocatedBlocks",
                "com.sk89q.worldedit.function.biome.BiomeReplace",
                "com.sk89q.worldedit.function.biome.ExtentBiomeCopy",
                "com.sk89q.worldedit.function.factory.Deform$DeformOperation",
                "com.sk89q.worldedit.function.factory.Paint",
                "com.sk89q.worldedit.function.mask.BiomeMask2D",
                "com.sk89q.worldedit.function.pattern.AbstractExtentPattern",
                "com.sk89q.worldedit.regions.iterator.RegionIterator",
                "com.sk89q.worldedit.regions.shape.ArbitraryShape",
                "com.sk89q.worldedit.function.factory.Apply",
                "com.sk89q.worldedit.function.visitor.RegionVisitor",
                "com.sk89q.worldedit.function.visitor.FlatRegionVisitor",
                "com.sk89q.worldedit.function.visitor.LayerVisitor"}) {
                modifyClasses(n, BaseRegionExtentSetter::new);
            }

            crateClass(CreatePlayerWrapper::new);
            crateClass(CreateNoPermsPlayer::new);
            crateClass(CreateNoPermsActor::new);
            crateClass(CreatePlayerFactory::new);
            crateClass(CreateActorFactory::new);

            logClean();
            return true;
        } catch (Throwable ex) {
            logDump();
            log("****************************");
            log("* CLASS INJECTION FAILED!! *");
            log("****************************");
            log("* AsyncWorldEdit won't work properly.");
            log("*");
            log("* >>>> Please make sure that you are using a supported version of world edit <<<< ");
            log("*");
            ExceptionHelper.printException(ex);
            log("****************************");

            try {
                Thread.sleep(10000);
            } catch (InterruptedException ex1) {
                // Ignore
            }

            return false;
        }
    }

    /**
     * Set new class factory
     *
     */
    public void setClassFactory(IClassFactory factory) {
        synchronized (m_mutex) {
            if (factory == null) {
                factory = new BaseClassFactory();
                log("New class factory set to default factory.");
            } else {
                log("New class factory set to: " + factory.getClass().getName());
            }

            m_classFactory = factory;
        }
    }

    /**
     * Get the class factory
     *
     */
    public IClassFactory getClassFactory() {
        return m_classFactory;
    }

    /**
     * getInjectorVersion The injector version
     *
     */
    public double getVersion() {
        return 2.0000;
    }

    private void modifyClasses(String className, Function<ClassWriter, InjectorClassVisitor> classVisitor) throws IOException {
        modifyClasses(className, classVisitor, 
                cn -> m_classInjector.getWorldEditClassReader(cn),
                (name, data) -> m_classInjector.injectWorldEditClass(name, data, 0, data.length));
    }
    
    private void modifyNMSClasses(String className, Function<ClassWriter, InjectorClassVisitor> classVisitor) throws IOException {
        modifyClasses(className, classVisitor, 
                cn -> m_classInjector.getNMSClassReader(cn),
                (name, data) -> m_classInjector.injectNMSClass(name, data, 0, data.length));
    }
    
    private void modifyClasses(String className, BiFunction<ClassWriter, ICreateClass, InjectorClassVisitor> classVisitor) throws IOException {
        modifyClasses(className, classVisitor, 
                cn -> m_classInjector.getWorldEditClassReader(cn),
                (name, data) -> m_classInjector.injectWorldEditClass(name, data, 0, data.length));
    }
    
    private void modifyNMSClasses(String className, BiFunction<ClassWriter, ICreateClass, InjectorClassVisitor> classVisitor) throws IOException {
        modifyClasses(className, classVisitor, 
                cn -> m_classInjector.getNMSClassReader(cn),
                (name, data) -> m_classInjector.injectNMSClass(name, data, 0, data.length));
    }
    
    private void modifyClasses(String className, Function<ClassWriter, InjectorClassVisitor> classVisitor,
            IGetClassReader getClassReader, IEmit emit) throws IOException {
        logQueue("Modify class " + className);
        
        ClassReader classReader = getClassReader.get(className);
        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

        InjectorClassVisitor icv = classVisitor.apply(classWriter);

        classReader.accept(icv, 0);
        icv.validate();

        byte[] data = classWriter.toByteArray();
        writeData(className , data);
        emit.emit(className, data);        
    }

    private void modifyClasses(String className, BiFunction<ClassWriter, ICreateClass, InjectorClassVisitor> classVisitor,
            IGetClassReader getClassReader, IEmit emit) throws IOException {
        logQueue("Modify class " + className);
                
        ClassReader classReader = getClassReader.get(className);
        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

        InjectorClassVisitor icv = classVisitor.apply(classWriter, (cn, cw) -> createClasses(cn, cw, emit));
        classReader.accept(icv, 0);
       
        icv.validate();

        byte[] data = classWriter.toByteArray();

        writeData(className , data);
        emit.emit(className, data);
    }
    
    private void crateClass(Function<ICreateClass, BaseClassCreator> factory) {
        crateClass(factory, 
                (name, data) -> m_classInjector.injectWorldEditClass(name, data, 0, data.length));
    }
    
    private void crateNMSClass(Function<ICreateClass, BaseClassCreator> factory) {
        crateClass(factory, 
                (name, data) -> m_classInjector.injectNMSClass(name, data, 0, data.length));
    }
    
    private void crateClass(Function<ICreateClass, BaseClassCreator> factory, IEmit emit) {
        BaseClassCreator bcc = factory.apply((cn, cw) -> createClasses(cn, cw, emit));
        
        logQueue("Creating class " + bcc.getName());
        bcc.run();
    }
    
    private void createClasses(String className, ClassWriter classWriter, IEmit emit) throws IOException {
        byte[] data = classWriter.toByteArray();

        writeData(className , data);
        emit.emit(className, data);
    }

    private void writeData(String className, byte[] data) {
        try (DataOutputStream dout = new DataOutputStream(new FileOutputStream(new File("./classes/" + className + ".class")))) {
            dout.write(data);
        } catch (IOException ex) {
            // Ignore this is only for debug :)
        }
    }

    @FunctionalInterface
    private interface IGetClassReader {
        ClassReader get(String className) throws IOException;
    }
    
    @FunctionalInterface
    private interface IEmit {
        void emit(String className, byte[] data) throws IOException;
    }

    private class NmsClassInjectorBridge implements IClassInjectorBridge {

        NmsClassInjectorBridge() { }

        @Override
        public void modifyClasses(String className, Function<ClassWriter, InjectorClassVisitor> classVisitor) throws IOException {
            modifyNMSClasses(m_classInjector.correctNmsName(className), classVisitor);
        }

        @Override
        public void modifyClasses(String className, BiFunction<ClassWriter, ICreateClass, InjectorClassVisitor> classVisitor) throws IOException {
            modifyNMSClasses(m_classInjector.correctNmsName(className), classVisitor);
        }

        @Override
        public void crateClass(Function<ICreateClass, BaseClassCreator> factory) {
            crateNMSClass(factory);
        }
    }
}
