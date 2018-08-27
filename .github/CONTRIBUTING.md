# Contributing to libGDX

Please take a moment to review this document in order to make the contribution
process easy and effective for everyone involved.

Following these guidelines helps to communicate that you respect the time of
the developers managing and developing this open source project. In return,
they should reciprocate that respect in addressing your issue or assessing
patches and features.


## Using the issue tracker

The issue tracker is the preferred channel for [bug reports](#bugs),
[feature requests](#features) and [submitting pull
requests](#pull-requests), but please respect the following restrictions:

* Please **do not** use the issue tracker for personal support requests (use
  [the forum](http://badlogicgames.com/forum/) or IRC). See also [Getting Help](https://github.com/libgdx/libgdx/wiki/Getting-Help).

* Please **do not** derail or troll issues. Keep the discussion on topic and
  respect the opinions of others.


<a name="bugs"></a>
## Bug reports

A bug is a _demonstrable problem_ that is caused by the code in the repository.
Good bug reports are extremely helpful - thank you!

Guidelines for bug reports:

1. **Use the GitHub issue search** &mdash; check if the issue has already been
   reported. Make sure to search all issues, not only the open issues.

2. **Check if the issue has been fixed** &mdash; try to reproduce it using the
   latest `master` or development branch in the repository.

3. **Isolate the problem** &mdash; create a [reduced test
   case](https://github.com/libgdx/libgdx/wiki/Getting-Help#executable-example-code).

A good bug report shouldn't leave others needing to chase you up for more
information. Please try to be as detailed as possible in your report. What is
your environment? What steps will reproduce the issue? What browser(s) and OS
experience the problem? What would you expect to be the outcome? All these
details will help people to fix any potential bugs.

If you have found a bug and want to fix it yourself immediately, great!
Create a [pull request](#pull-requests) with your proposed correction and
a description of the problem you are fixing. Please do **not** create a separate
issue for the bug report, the pull request is enough.

See [Getting Help](https://github.com/libgdx/libgdx/wiki/Getting-Help) for more information and an example.


<a name="features"></a>
## Feature requests

Feature requests are welcome. But take a moment to find out whether your idea
fits with the scope and aims of the project. It's up to *you* to make a strong
case to convince the project's developers of the merits of this feature. Please
provide as much detail and context as possible.

<a name="pull-requests"></a>
## Pull requests

Contributing to libGDX is easy:

  * Fork libGDX on [`http://github.com/libgdx/libgdx`](http://github.com/libgdx/libgdx)
  * Learn how to [Work with the Source](https://github.com/libgdx/libgdx/wiki/Running-demos-%26-tests)
  * Hack away, and send a pull request on GitHub!

### API Changes & Additions
If you modify a public API, or add a new one, make sure to add these changes to the [CHANGES](https://github.com/libgdx/libgdx/blob/master/CHANGES) file in the root of the repository. In addition to the CHANGES file, such modifications are also published on the [blog](http://www.badlogicgames.com) and on [Twitter](http://www.twitter.com/badlogicgames) to reach all of the community.

If you want to poll the brains of other devs, either send a pull request and start a conversation on Github, or start a new thread in [this sub-forum](http://www.badlogicgames.com/forum/viewforum.php?f=23). You will need special forum permissions, write an e-mail to contact at badlogicgames dot com and tell me your forum id. You should also subscribe to that forum via e-mail, there's a button at the bottom of the page. You can also drop by on IRC (irc.freenode.org, #libgdx), where most core devs are lurking.

### Contributor License Agreement

Libgdx is licensed under the [Apache 2.0 license](http://en.wikipedia.org/wiki/Apache_License). Before we can accept code contributions, we need you to sign our [contributor license agreement](https://github.com/libgdx/libgdx/blob/master/CLA.txt). Just print it out, fill in the blanks and send a copy to [`contact@badlogicgames.com`](mailto:contact@badlogicgames.com?subject=[LibGDX]%20CLA), with the subject `[Libgdx] CLA`.

Signing the CLA will allow us to use and distribute your code. This is a non-exclusive license, so you retain all rights to your code. It's a fail-safe for us should someone contribute essential code and later decide to take it back.

### Eclipse Formatter

If you work on libGDX code, we require you to use the [Eclipse formatter](https://github.com/libgdx/libgdx/blob/master/eclipse-formatter.xml) located in the root directory of the repository.

Failure to use the formatter will result in Nate being very upset.

If you are using IntelliJ IDEA, you can still make use of the eclipse code formatter. See [this article](http://blog.jetbrains.com/idea/2014/01/intellij-idea-13-importing-code-formatter-settings-from-eclipse/?utm_source=hootsuite&utm_campaign=hootsuite) for more information.

### Code Style

LibGDX does not have an official coding standard. We mostly follow the usual [Java style](http://www.oracle.com/technetwork/java/codeconvtoc-136057.html), and so should you.

A few things we'd rather not like to see:

  * underscores in any kind of identifier
  * [Hungarian notation](http://en.wikipedia.org/wiki/Hungarian_notation)
  * Prefixes for fields or arguments
  * Curlies on new lines
  * Conditional block bodies without curlies when the block spans more than one line

If you modify an existing file, follow the style of the code in there.

If you create a new file, make sure to add the Apache file header, as seen [here](https://github.com/libgdx/libgdx/blob/master/gdx/src/com/badlogic/gdx/Application.java).

If you create a new class, please add at least class documentation that explains the usage and scope of he class. You can omit Javadoc for methods that are self-explanatory.

If your class is explicitly thread-safe, mention it in the Javadoc. The default assumption is that classes are not thread-safe, to reduce the amount of costly locks in the code base.

### Performance Considerations

LibGDX is meant to run on both desktop and mobile platforms, including browsers (JavaScript!). While the desktop HotSpot VM can take quite a beating in terms of unnecessary allocations, Dalvik and consorts don't.

A couple of guidelines:

  * Avoid temporary object allocation wherever possible
  * Do not make defensive copies
  * Avoid locking. libGDX classes are, by default, not thread-safe unless explicitly specified
  * Do not use boxed primitives
  * Use the collection classes in the [`com.badlogic.gdx.utils` package](https://github.com/libgdx/libgdx/tree/master/gdx/src/com/badlogic/gdx/utils)
  * Do not perform argument checks for methods that may be called thousands of times per frame
  * Use pooling if necessary, if possible, avoid exposing the pooling to the user as it complicates the API

### Git

Most of the libGDX team members are Git novices. As such, we are just learning the ropes ourselves. To lower the risk of getting something wrong, we'd kindly ask you to keep your pull requests small if possible. A changeset of 3000 files is likely not to get merged.

We do open new branches for bigger API changes. If you help out with a new API, make sure your pull request targets that specific branch.

Pull requests for the master repository will be checked by multiple core contributors before inclusion. We may reject your pull request to `master` if we do not deem them to be ready or fitting. Please don't take offense in that case. LibGDX is used by thousands of projects around the world. We need to make sure things stay somewhat sane and stable.
