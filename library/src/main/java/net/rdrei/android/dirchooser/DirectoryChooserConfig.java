package net.rdrei.android.dirchooser;

import android.os.Parcelable;

import auto.parcel.AutoParcel;

@AutoParcel
public abstract class DirectoryChooserConfig implements Parcelable {
    /**
     * @return Builder for a new DirectoryChooserConfig.
     */
    public static Builder builder() {
        return new AutoParcel_DirectoryChooserConfig.Builder()
                .initialDirectory("")
                .allowNewDirectoryNameModification(false)
                .allowReadOnlyDirectory(false);
    }

    /**
     * Name of the directory to create. User can change this name when he creates the
     * folder. To avoid this use {@link #allowNewDirectoryNameModification} argument.
     */
    abstract String newDirectoryName();

    /**
     * Optional argument to define the path of the directory
     * that will be shown first.
     * If it is not sent or if path denotes a non readable/writable directory
     * or it is not a directory, it defaults to
     * {@link android.os.Environment#getExternalStorageDirectory()}
     */
    abstract String initialDirectory();

    /**
     * Argument to define whether or not the directory chooser
     * allows read-only paths to be chosen. If it false only
     * directories with read-write access can be chosen.
     */
    abstract boolean allowReadOnlyDirectory();


    /**
     * Argument to define whether or not the directory chooser
     * allows modification of provided new directory name.
     */
    abstract boolean allowNewDirectoryNameModification();

    @AutoParcel.Builder
    public abstract static class Builder {
        public abstract Builder newDirectoryName(String s);
        public abstract Builder initialDirectory(String s);
        public abstract Builder allowReadOnlyDirectory(boolean b);
        public abstract Builder allowNewDirectoryNameModification(boolean b);
        public abstract DirectoryChooserConfig build();
    }
}
