/*
 * JEquity
 * Copyright(c) 2008-2019, Beowurks
 * Original Author: Eddie Fann
 * License: Eclipse Public License - v 2.0 (https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html)
 *
 */

package com.beowurks.jequity.utility;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.text.DateFormat;
import java.util.Properties;

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
public class BaseProperties
{
  // From http://stackoverflow.com/questions/25087094/java-cipher-update-method
  private final static String CRYPTO_PROVIDER_BASE = "AES";
  private final static String CRYPTO_PROVIDER = "AES";
  private final static int CRYPTO_KEY_LENGTH = 16;

  private final Properties foProperties = new Properties();
  private final DateFormat foDateFormat = DateFormat.getDateTimeInstance();

  private String fcFileName;
  private String fcDirectory;
  private String fcFullName;
  private String fcLockFullName;
  private String fcHeader;

  private boolean flLockActive = false;

  private File foLockFile = null;

  private String fcKey = null;

  private boolean flDisplayErrors = false;

  // ---------------------------------------------------------------------------------------------------------------------
  public BaseProperties(final String tcDirectory, final String tcFileName, final String tcHeader)
  {
    this.initialize(tcDirectory, tcFileName, tcHeader, null, true, false);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  // Encrypted properties file.
  public BaseProperties(final String tcDirectory, final String tcFileName, final String tcHeader, final String tcKey)
  {
    this.initialize(tcDirectory, tcFileName, tcHeader, tcKey, true, false);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  // Encrypted properties file.
  public BaseProperties(final String tcDirectory, final String tcFileName, final String tcHeader, final String tcKey, final boolean tlReadProperties)
  {
    this.initialize(tcDirectory, tcFileName, tcHeader, tcKey, tlReadProperties, false);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  // Encrypted properties file.
  public BaseProperties(final String tcDirectory, final String tcFileName, final String tcHeader, final String tcKey, final boolean tlReadProperties, final boolean tlDisplayErrors)
  {
    this.initialize(tcDirectory, tcFileName, tcHeader, tcKey, tlReadProperties, tlDisplayErrors);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  // All options.
  private void initialize(final String tcDirectory, final String tcFileName, final String tcHeader, final String tcKey, final boolean tlReadProperties, final boolean tlDisplayErrors)
  {
    if (!Misc.makeDirectory(tcDirectory))
    {
      Misc.errorMessage("Unable to create the directory of " + tcDirectory + ".");
    }

    this.fcDirectory = tcDirectory;
    this.fcFileName = tcFileName;
    this.fcHeader = tcHeader;
    this.fcKey = tcKey;

    this.flDisplayErrors = tlDisplayErrors;

    this.fcFullName = Misc.includeTrailingBackslash(tcDirectory) + this.fcFileName;
    this.fcLockFullName = this.fcFullName + ".lck";

    if (tlReadProperties)
    {
      this.readProperties();
    }

  }

  // ---------------------------------------------------------------------------------------------------------------------
  public boolean copyToFile(final String tcDirectory, final String tcFileName)
  {
    boolean llOkay = true;
    final String lcCopyFullName = Misc.includeTrailingBackslash(tcDirectory) + tcFileName;
    FileOutputStream loFileOutputStream = null;
    try
    {
      loFileOutputStream = new FileOutputStream(lcCopyFullName);
      this.foProperties.store(loFileOutputStream, this.fcHeader);
      loFileOutputStream.close();
    }
    catch (final Exception loErr)
    {
      llOkay = false;
      Misc.infoMessage(loErr.getMessage());
    }

    return (llOkay);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public boolean createLock()
  {
    boolean llOkay = true;

    if (!this.flLockActive)
    {
      this.foLockFile = new File(this.fcLockFullName);
      this.foLockFile.deleteOnExit();

      try
      {
        llOkay = this.foLockFile.createNewFile();
      }
      catch (final IOException loErr)
      {
        llOkay = false;
      }

      this.flLockActive = llOkay;
    }

    return (llOkay);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public boolean releaseLock()
  {
    boolean llOkay = true;

    if (this.flLockActive)
    {
      if (this.foLockFile != null)
      {
        if (this.foLockFile.exists())
        {
          try
          {
            this.foLockFile.delete();
          }
          catch (final SecurityException ignored)
          {
          }
        }

        llOkay = !this.foLockFile.exists();
        if (llOkay)
        {
          this.foLockFile = null;
        }
      }

      this.flLockActive = !llOkay;
    }

    return (llOkay);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setKey(final String tcKey)
  {
    this.fcKey = tcKey;
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public boolean fileExists()
  {
    return ((new File(this.fcFullName)).exists());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public boolean readProperties()
  {
    byte[] lbInput = null;
    if (this.fileExists())
    {
      lbInput = Misc.binaryFileToBytes(this.fcFullName);
    }

    this.foProperties.clear();
    if (lbInput == null)
    {
      return (true);
    }

    boolean llOkay = true;
    if (this.fcKey == null)
    {
      try
      {
        this.foProperties.load(new StringReader(new String(lbInput, "UTF8")));
      }
      catch (final IOException loErr)
      {
        llOkay = false;
        Misc.infoMessage(loErr.getMessage());
      }
    }
    else
    {
      final SecretKeySpec loKey = new SecretKeySpec(Misc.getKeyBytes(this.fcKey, BaseProperties.CRYPTO_KEY_LENGTH), BaseProperties.CRYPTO_PROVIDER_BASE);
      try
      {
        final Cipher loCipher = Cipher.getInstance(BaseProperties.CRYPTO_PROVIDER);

        loCipher.init(Cipher.DECRYPT_MODE, loKey);

        this.foProperties.load(new StringReader(new String(loCipher.doFinal(lbInput), "UTF8")));
      }
      catch (final IOException loErr)
      {
        llOkay = false;
        Misc.infoMessage(loErr.getMessage());
      }
      // Let's not display any encryption-related messages to the end-user.
      catch (final InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException loErr)
      {
        llOkay = false;
        if (this.flDisplayErrors)
        {
          loErr.printStackTrace();
        }
      }
    }

    return (llOkay);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public boolean writeProperties()
  {
    boolean llOkay = true;
    final StringWriter loStringWriter = new StringWriter();

    try
    {
      this.foProperties.store(loStringWriter, this.fcHeader);
    }
    catch (final Exception loErr)
    {
      llOkay = false;
      Misc.infoMessage(loErr.getMessage());
    }

    final String lcString = loStringWriter.getBuffer().toString();

    if (this.fcKey == null)
    {
      try
      {
        Misc.bytesToBinaryFile(lcString.getBytes("UTF8"), this.fcFullName);
      }
      catch (final UnsupportedEncodingException loErr)
      {
        llOkay = false;
        Misc.infoMessage(loErr.getMessage());
      }
    }
    else
    {
      try
      {
        final byte[] lbOutput = lcString.getBytes("UTF8");
        final SecretKeySpec loKey = new SecretKeySpec(Misc.getKeyBytes(this.fcKey, BaseProperties.CRYPTO_KEY_LENGTH), BaseProperties.CRYPTO_PROVIDER_BASE);
        final Cipher loCipher = Cipher.getInstance(BaseProperties.CRYPTO_PROVIDER);

        loCipher.init(Cipher.ENCRYPT_MODE, loKey);

        Misc.bytesToBinaryFile(loCipher.doFinal(lbOutput), this.fcFullName);
      }
      catch (final InvalidKeyException | UnsupportedEncodingException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException loErr)
      {
        llOkay = false;
        if (this.flDisplayErrors)
        {
          loErr.printStackTrace();
        }
      }
    }

    return (llOkay);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public boolean deleteFile()
  {
    final File loFile = new File(this.fcFullName);

    return (loFile.delete());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public String getDirectory()
  {
    return (this.fcDirectory);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public String getFileName()
  {
    return (this.fcFileName);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public String getFullName()
  {
    return (this.fcFullName);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public boolean getProperty(final String tcKey, final boolean tlDefault)
  {
    final String lcValue = this.foProperties.getProperty(tcKey);
    if (lcValue == null)
    {
      return (tlDefault);
    }

    return (Boolean.valueOf(lcValue).booleanValue());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public double getProperty(final String tcKey, final double tnDefault)
  {
    double lnValue = tnDefault;

    final String lcValue = this.foProperties.getProperty(tcKey);
    if (lcValue != null)
    {
      try
      {
        lnValue = Double.parseDouble(lcValue);
      }
      catch (final NumberFormatException loErr)
      {
        lnValue = tnDefault;
      }
    }

    return (lnValue);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public Color getProperty(final String tcKey, final Color toDefault)
  {
    return (new Color(this.getProperty(tcKey, toDefault.getRGB())));
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public int getProperty(final String tcKey, final int tnDefault)
  {
    int lnValue = tnDefault;
    final String lcValue = this.foProperties.getProperty(tcKey);

    if (lcValue != null)
    {
      try
      {
        lnValue = Integer.parseInt(lcValue);
      }
      catch (final NumberFormatException loErr)
      {
        lnValue = tnDefault;
      }
    }

    return (lnValue);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public Date getProperty(final String tcKey, final Date tdDefault)
  {
    Date ldValue = tdDefault;

    final String lcValue = this.foProperties.getProperty(tcKey);
    if (lcValue != null)
    {
      try
      {
        ldValue = Date.valueOf(lcValue);
      }
      catch (final Exception loErr)
      {
        ldValue = tdDefault;
      }
    }

    return (ldValue);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public long getProperty(final String tcKey, final long tnDefault)
  {
    long lnValue = tnDefault;
    final String lcValue = this.foProperties.getProperty(tcKey);

    if (lcValue != null)
    {
      try
      {
        lnValue = Long.parseLong(lcValue);
      }
      catch (final NumberFormatException loErr)
      {
        lnValue = tnDefault;
      }
    }

    return (lnValue);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public String getProperty(final String tcKey, final String tcDefault)
  {
    final String lcValue = this.foProperties.getProperty(tcKey);
    if (lcValue == null)
    {
      return (tcDefault);
    }

    return (lcValue);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setProperty(final String tcKey, final boolean tlValue)
  {
    this.foProperties.put(tcKey, Boolean.toString(tlValue));
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setProperty(final String tcKey, final double tnValue)
  {
    this.foProperties.put(tcKey, Double.toString(tnValue));
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setProperty(final String tcKey, final Color toValue)
  {
    this.setProperty(tcKey, toValue.getRGB());
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setProperty(final String tcKey, final int tnValue)
  {
    this.foProperties.put(tcKey, Integer.toString(tnValue));
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setProperty(final String tcKey, final Date tdValue)
  {
    this.foProperties.put(tcKey, this.foDateFormat.format(tdValue));
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setProperty(final String tcKey, final long tnValue)
  {
    this.foProperties.put(tcKey, Long.toString(tnValue));
  }

  // ---------------------------------------------------------------------------------------------------------------------
  public void setProperty(final String tcKey, final String tcValue)
  {
    this.foProperties.put(tcKey, tcValue);
  }

  // ---------------------------------------------------------------------------------------------------------------------
}
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
